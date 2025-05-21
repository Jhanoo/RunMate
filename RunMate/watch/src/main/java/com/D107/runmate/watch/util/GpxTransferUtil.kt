package com.D107.runmate.watch.presentation.util

import android.content.Context
import android.util.Log
import com.D107.runmate.watch.domain.model.GpxFile
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

object GpxTransferUtil {
    private const val TAG = "GpxTransferUtil"

    suspend fun uploadGpxFile(context: Context, gpxFile: GpxFile): Boolean = withContext(Dispatchers.IO) {
        try {
            // 파일 경로로부터 File 객체 생성
            val file = File(gpxFile.filePath)
            if (!file.exists()) {
                Log.e(TAG, "GPX file not found: ${gpxFile.filePath}")
                return@withContext false
            }

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

                val startLocation = extractStartLocationFromGpx(file)
                dataMap.putString("startLocation", startLocation)

                dataMap.putString("groupId", "")
                dataMap.putInt("avgCadence", gpxFile.avgCadence)
            }

            // 데이터 전송
            val dataClient = Wearable.getDataClient(context)
            val putDataReq = request.asPutDataRequest()
            putDataReq.setUrgent()

            val result = Tasks.await(dataClient.putDataItem(putDataReq))

            if (result != null) {
                Log.d(TAG, "Successfully sent GPX file: ${file.name}")
                return@withContext true
            } else {
                Log.e(TAG, "Failed to send GPX file: ${file.name}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending GPX file: ${e.message}", e)
            return@withContext false
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
    private fun extractStartLocationFromGpx(gpxFile: File): String {
        try {
            // XML 파싱을 위한 간단한 방법
            val fileContent = gpxFile.readText()

            // <trkpt> 태그를 찾아 첫 번째 위치 추출
            val pattern = """<trkpt\s+lat="([^"]+)"\s+lon="([^"]+)"""".toRegex()
            val matchResult = pattern.find(fileContent)

            if (matchResult != null && matchResult.groupValues.size >= 3) {
                val lat = matchResult.groupValues[1]
                val lon = matchResult.groupValues[2]
                return "$lat,$lon"
            }

            Log.w(TAG, "No track points found in GPX file")
            return ""
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting start location from GPX file", e)
            return ""
        }
    }
}