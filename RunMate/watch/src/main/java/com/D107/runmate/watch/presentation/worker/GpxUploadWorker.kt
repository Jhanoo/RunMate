package com.D107.runmate.watch.presentation.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.D107.runmate.watch.domain.model.GpxFile
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.D107.runmate.watch.domain.repository.GpxRepository
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class GpxUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gpxRepository: GpxRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "GpxUploadWorker"
        private const val WORK_NAME = "gpx_upload_work"
        private const val KEY_FILE_ID = "file_id"

        // 주기적인 업로드 체크 스케줄링 (15분마다)
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<GpxUploadWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        // 특정 파일 즉시 업로드 시도
        fun uploadFile(context: Context, fileId: Long) {
            val data = workDataOf(KEY_FILE_ID to fileId)
            val request = OneTimeWorkRequestBuilder<GpxUploadWorker>()
                .setInputData(data)
                .setConstraints(androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "upload_file_$fileId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        val fileId = inputData.getLong(KEY_FILE_ID, -1L)

        return try {
            if (fileId != -1L) {
                // 특정 파일 업로드
                uploadSpecificFile(fileId)
            } else {
                // 모든 대기 중인 파일 업로드
                uploadPendingFiles()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "업로드 작업 실패: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun uploadSpecificFile(fileId: Long): Boolean {
        Log.d(TAG, "파일 ID: $fileId 폰으로 전송 시작")
        // 상태를 UPLOADING으로 업데이트
        gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.UPLOADING)
        // DB에서 파일 정보 조회
        val gpxFileInfo = gpxRepository.getGpxFileById(fileId) ?: return false
        val file = File(gpxFileInfo.filePath)

        if (!file.exists()) {
            Log.e(TAG, "파일이 존재하지 않음: ${file.absolutePath}")
            gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.FAILED)
            return false
        }
        // 폰으로 데이터 전송 시도
        val isTransferred = transferToPhone(file, gpxFileInfo)
        // 전송 결과에 따라 상태 업데이트
        if (isTransferred) {
            gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.SUCCESS)
            Log.d(TAG, "파일 폰 전송 성공: $fileId")
        } else {
            gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.FAILED)
            Log.e(TAG, "파일 폰 전송 실패: $fileId")
        }
        return isTransferred
    }

    private suspend fun uploadPendingFiles(): Boolean {
        Log.d(TAG, "대기 중인 모든 파일 업로드 시작")

        val pendingFiles = gpxRepository.getPendingGpxFiles().first()
        if (pendingFiles.isEmpty()) {
            Log.d(TAG, "업로드할 파일이 없음")
            return true
        }

        var allSuccess = true

        for (gpxFile in pendingFiles) {
            val success = uploadSpecificFile(gpxFile.id)
            if (!success) {
                allSuccess = false
            }
        }

        return allSuccess
    }

    // 폰에 파일 전송하는 함수 추가
    private fun transferToPhone(file: File, gpxFileInfo: GpxFile): Boolean {
        try {
            // 데이터 클라이언트 생성
            val dataClient = Wearable.getDataClient(applicationContext)

            // 파일 내용 읽기
            val fileContent = file.readBytes()

            // 결과 데이터 생성
            val resultData = PutDataMapRequest.create("/running_result").apply {
                dataMap.putDouble("avgPace", convertPaceToDouble(gpxFileInfo.avgPace))
                dataMap.putDouble("calories", 0.0) // 칼로리는 0.0으로 고정
                dataMap.putString("endTime", formatISODate(gpxFileInfo.endTime))
                dataMap.putDouble("avgElevation", gpxFileInfo.avgElevation)
                dataMap.putDouble("avgBpm", gpxFileInfo.avgHeartRate.toDouble())
                dataMap.putString("startTime", formatISODate(gpxFileInfo.startTime))
                dataMap.putDouble("distance", gpxFileInfo.totalDistance)
                dataMap.putString("courseId", "") // 빈 문자열
                dataMap.putString("startLocation", "") // 빈 문자열
                dataMap.putString("groupId", "") // 빈 문자열
                dataMap.putDouble("avgCadence", gpxFileInfo.avgCadence.toDouble())

                // GPX 파일 바이너리 데이터
                dataMap.putByteArray("gpxFile", fileContent)
            }

            val request = resultData.asPutDataRequest()
            request.setUrgent()

            // 데이터 전송
            val task = dataClient.putDataItem(request)
            return Tasks.await(task) != null
        } catch (e: Exception) {
            Log.e("GpxTransfer", "파일 전송 중 오류 발생: ${e.message}", e)
            return false
        }
    }

    // 보조 함수: 페이스 문자열을 Double로 변환
    private fun convertPaceToDouble(gpxFileInfo: String): Double {
        // 예: "5'30\"" -> 5.5
        try {
            val avgPace = "5.23" // 임시값, 실제로는 계산 필요
            return avgPace.toDouble()
        } catch (e: Exception) {
            return 0.0
        }
    }

    // 보조 함수: 칼로리 계산 (간단한 추정)
    private fun calculateCalories(gpxFileInfo: GpxFile): Double {
        // 평균적인 사람이 1km 달릴 때 약 60kcal 소모
        return gpxFileInfo.totalDistance * 60.0
    }

    // ISO 8601 형식으로 날짜 포맷팅
    private fun formatISODate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        return sdf.format(date)
    }
}