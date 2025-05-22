package com.D107.runmate.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GpxFileStorage {
    private const val TAG = "GpxFileStorage"

    /**
     * 워치로부터 받은 GPX 에셋을 파일로 저장합니다.
     */
    suspend fun saveGpxFile(
        context: Context,
        gpxAsset: Asset,
        distance: Double,
        startTime: Long,
        startLocation: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Asset에서 데이터 얻기
            val dataClient = Wearable.getDataClient(context)
            val assetFdResult = Tasks.await(dataClient.getFdForAsset(gpxAsset))
            val assetInputStream = assetFdResult.inputStream

            if (assetInputStream == null) {
                Timber.e("Failed to get asset input stream")
                return@withContext null
            }

            // 외부 저장소 디렉토리 확인 및 생성
            val gpxDir = File(context.getExternalFilesDir(null), "gpx_files")
            if (!gpxDir.exists()) {
                gpxDir.mkdirs()
            }

            // 파일명 생성 (타임스탬프, 거리 포함)
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateStr = dateFormat.format(Date(startTime))
            val distanceStr = String.format(Locale.US, "%.2f", distance)
            val fileName = "run_${dateStr}_${distanceStr}km.gpx"

            val gpxFile = File(gpxDir, fileName)

            // 파일에 데이터 쓰기
            FileOutputStream(gpxFile).use { outputStream ->
                assetInputStream.copyTo(outputStream)
            }

            // 메타데이터 파일 생성
            val metaFile = File(gpxDir, "${fileName}.meta")
            FileOutputStream(metaFile).use { outputStream ->
                val metaData = """
                    timestamp: $startTime
                    date: ${Date(startTime)}
                    distance: $distance km
                    startLocation: $startLocation
                """.trimIndent()
                outputStream.write(metaData.toByteArray())
            }

            Timber.d("GPX file saved: ${gpxFile.absolutePath}")

            // MediaScanner에 파일 추가 (갤러리 등에서 볼 수 있게)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.fromFile(gpxFile)
            })

            return@withContext gpxFile.absolutePath

        } catch (e: Exception) {
            Timber.e("Error saving GPX file: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
}