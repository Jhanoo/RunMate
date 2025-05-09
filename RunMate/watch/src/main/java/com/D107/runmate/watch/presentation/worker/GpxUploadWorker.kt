package com.D107.runmate.watch.presentation.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.D107.runmate.watch.domain.repository.GpxRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
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
        Log.d(TAG, "파일 ID: $fileId 업로드 시작")

        // 상태를 UPLOADING으로 업데이트
        gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.UPLOADING)

        // DB에서 파일 정보 조회
        val file = File(gpxRepository.getGpxFileById(fileId)?.filePath ?: return false)
        if (!file.exists()) {
            Log.e(TAG, "파일이 존재하지 않음: ${file.absolutePath}")
            gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.FAILED)
            return false
        }

        // 파일 업로드 시도
        val uploadSuccess = gpxRepository.uploadGpxFile(file)

        // 업로드 결과에 따라 상태 업데이트
        if (uploadSuccess) {
            gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.SUCCESS)
            Log.d(TAG, "파일 업로드 성공: $fileId")
        } else {
            gpxRepository.updateGpxFileStatus(fileId, GpxUploadStatus.FAILED)
            Log.e(TAG, "파일 업로드 실패: $fileId")
        }

        return uploadSuccess
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
}