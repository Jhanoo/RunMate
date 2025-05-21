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
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.D107.runmate.watch.domain.repository.GpxRepository
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

@HiltWorker
class GpxUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gpxRepository: GpxRepository
) : CoroutineWorker(context, workerParams) {

    private val dataClient: DataClient = Wearable.getDataClient(context)


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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 전송 대기 중인 GPX 파일 목록 가져오기
            val pendingFiles = gpxRepository.getPendingGpxFiles().first()
            Log.d(TAG, "Found ${pendingFiles.size} pending GPX files")

            if (pendingFiles.isEmpty()) {
                return@withContext Result.success()
            }

            pendingFiles.forEach { gpxFile ->
                try {
                    // 파일 경로로부터 File 객체 생성
                    val file = File(gpxFile.filePath)
                    if (!file.exists()) {
                        Log.e(TAG, "GPX file not found: ${gpxFile.filePath}")
                        gpxRepository.updateGpxFileStatus(gpxFile.id, GpxUploadStatus.FAILED)
                        return@forEach
                    }

                    // 상태를 UPLOADING으로 변경
                    gpxRepository.updateGpxFileStatus(gpxFile.id, GpxUploadStatus.UPLOADING)

                    // 파일을 Asset으로 변환
                    val asset = createAssetFromFile(file)

                    // DataMap에 추가하여 전송 준비
                    val request = PutDataMapRequest.create("/gpx_file").apply {
                        dataMap.putAsset("gpx_asset", asset)
                        dataMap.putLong("id", gpxFile.id)
                        dataMap.putLong("timestamp", System.currentTimeMillis())

                        dataMap.putDouble("distance", gpxFile.totalDistance)

                        val paceDouble = convertPaceStringToDouble(gpxFile.avgPace)
                        dataMap.putDouble("avgPace", paceDouble)

                        dataMap.putString("calories", "")
                        dataMap.putLong("startTime", gpxFile.startTime.time)
                        dataMap.putLong("endTime", gpxFile.endTime.time)

                        dataMap.putDouble("avgElevation", gpxFile.avgElevation)
                        dataMap.putInt("avgBpm", gpxFile.avgHeartRate)
                        dataMap.putString("courseId", "")

                        val startLat = extractStartLocationFromGpx(file, "lat")
                        dataMap.putString("startLat", startLat)
                        val startLon = extractStartLocationFromGpx(file, "lon")
                        dataMap.putString("startLon", startLon)

                        dataMap.putString("groupId", "")
                        dataMap.putInt("avgCadence", gpxFile.avgCadence)
                    }

                    // 데이터 전송
                    val putDataReq = request.asPutDataRequest()
                    putDataReq.setUrgent()
                    val result = Tasks.await(dataClient.putDataItem(putDataReq))

                    if (result != null) {
                        Log.d(TAG, "Successfully sent GPX file: ${file.name}")
                        gpxRepository.updateGpxFileStatus(gpxFile.id, GpxUploadStatus.SUCCESS)
                    } else {
                        Log.e(TAG, "Failed to send GPX file: ${file.name}")
                        gpxRepository.updateGpxFileStatus(gpxFile.id, GpxUploadStatus.FAILED)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending GPX file: ${e.message}", e)
                    gpxRepository.updateGpxFileStatus(gpxFile.id, GpxUploadStatus.FAILED)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}", e)
            Result.failure()
        }
    }

    private suspend fun createAssetFromFile(file: File): Asset = withContext(Dispatchers.IO) {
        // 파일 읽기
        val fileSize = file.length().toInt()
        val bytes = ByteArray(fileSize)

        FileInputStream(file).use { inputStream ->
            var bytesRead = 0
            var result: Int
            while (bytesRead < fileSize) {
                result = inputStream.read(bytes, bytesRead, fileSize - bytesRead)
                if (result == -1) break
                bytesRead += result
            }
        }

        // ByteBuffer로 변환 후 Asset 생성
        val buffer = ByteBuffer.allocate(fileSize)
        buffer.put(bytes)
        buffer.position(0)
        Asset.createFromBytes(buffer.array())
    }

    // 페이스 문자열을 Double로 변환하는 함수 (예: "5'23"" -> 5.23)
    private fun convertPaceStringToDouble(paceString: String): Double {
        try {
            // 페이스 포맷이 "5'23""와 같은 형식이라면:
            val parts = paceString.split("'", "'\"", "\"")
            if (parts.size >= 2) {
                val minutes = parts[0].toDoubleOrNull() ?: 0.0
                val seconds = parts[1].toDoubleOrNull() ?: 0.0

                // 초를 소수점 형태로 변환 (초/60 = 분의 소수점 부분)
                val secondsAsFraction = seconds / 60.0

                // 소수점 두 자리로 반올림
                return String.format("%.2f", minutes + secondsAsFraction).toDouble()
            }

            // "5:23" 형식이라면:
            if (paceString.contains(":")) {
                val parts = paceString.split(":")
                if (parts.size >= 2) {
                    val minutes = parts[0].toDoubleOrNull() ?: 0.0
                    val seconds = parts[1].toDoubleOrNull() ?: 0.0

                    val secondsAsFraction = seconds / 60.0
                    return String.format("%.2f", minutes + secondsAsFraction).toDouble()
                }
            }

            // 숫자만 있는 경우 그대로 반환
            return paceString.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            Log.e(TAG, "Error converting pace: $paceString", e)
            return 0.0
        }
    }

    // GPX 파일에서 시작 위치를 추출하는 함수
       private fun extractStartLocationFromGpx(gpxFile: File, type: String ): String {
        try {
            // XML 파싱을 위한 간단한 방법
            val fileContent = gpxFile.readText()

            // <trkpt> 태그를 찾아 첫 번째 위치 추출
            val pattern = """<trkpt\s+lat="([^"]+)"\s+lon="([^"]+)"""".toRegex()
            val matchResult = pattern.find(fileContent)

            if (matchResult != null && matchResult.groupValues.size >= 3) {
                val lat = matchResult.groupValues[1]
                val lon = matchResult.groupValues[2]

                if(type == "lat") {
                    return lat
                } else if (type == "lon") {
                    return lon
                }
            }

            Log.w(TAG, "No track points found in GPX file")
            return ""
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting start location from GPX file", e)
            return ""
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