package com.D107.runmate.watch.data.repository

import android.content.Context
import android.util.Log
import com.D107.runmate.watch.data.local.GpxDao
import com.D107.runmate.watch.data.local.GpxEntity
import com.D107.runmate.watch.data.remote.GpxApiService
import com.D107.runmate.watch.domain.model.GpxFile
import com.D107.runmate.watch.domain.model.GpxMetadata
import com.D107.runmate.watch.domain.model.GpxTrackPoint
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.D107.runmate.watch.domain.repository.GpxRepository
import com.D107.runmate.watch.util.GpxGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gpxDao: GpxDao,
    private val gpxApiService: GpxApiService
) : GpxRepository {

    // 현재 세션의 트랙 포인트 저장소 (메모리)
    private val sessionTrackPoints = mutableListOf<GpxTrackPoint>()

    override suspend fun addTrackPoint(trackPoint: GpxTrackPoint) {
        sessionTrackPoints.add(trackPoint)
        Log.d("GpxTracking", "트랙 포인트 추가됨: 현재 총 ${sessionTrackPoints.size}개 포인트")
    }

    override suspend fun getSessionTrackPoints(): List<GpxTrackPoint> {
        Log.d("GpxTracking", "세션 트랙 포인트 조회: ${sessionTrackPoints.size}개")
        return sessionTrackPoints.toList()
    }

    override suspend fun clearSession() {
        val count = sessionTrackPoints.size
        sessionTrackPoints.clear()
        Log.d("GpxTracking", "세션 트랙 포인트 초기화: ${count}개 삭제됨")
    }

    override suspend fun createGpxFile(metadata: GpxMetadata): File {
        Log.d("GpxTracking", "GPX 파일 생성 시작: 트랙 포인트 ${sessionTrackPoints.size}개")

        // GPX 디렉토리 확인 및 생성
        val gpxDir = context.getExternalFilesDir("gpx")
            ?: throw IllegalStateException("외부 저장소에 접근할 수 없습니다")

        if (!gpxDir.exists()) {
            gpxDir.mkdirs()
            Log.d("GpxTracking", "GPX 디렉토리 생성: ${gpxDir.absolutePath}")
        }

        // 파일명 생성 (timestamp_런닝.gpx)
        val timestamp = metadata.startTime.time
        val fileName = "${timestamp}_running.gpx"
        val gpxFile = File(gpxDir, fileName)

        Log.d("GpxTracking", "GPX 파일 경로: ${gpxFile.absolutePath}")

        // GPX 파일 생성
        val success = GpxGenerator.createGpxFile(
            file = gpxFile,
            trackPoints = sessionTrackPoints,
            metadata = metadata
        )

        Log.d("GpxTracking", "GPX 파일 생성 " + if (success) "성공" else "실패")

        return gpxFile
    }

    override suspend fun saveGpxFileInfo(gpxFile: GpxFile): Long {
        val entity = GpxEntity(
            id = 0, // 자동 생성
            filePath = gpxFile.filePath,
            status = gpxFile.status.name,
            createdAt = gpxFile.createdAt.time,
            lastAttempt = gpxFile.lastAttempt?.time,
            totalDistance = gpxFile.totalDistance,
            totalTime = gpxFile.totalTime,
            avgHeartRate = gpxFile.avgHeartRate,
            maxHeartRate = gpxFile.maxHeartRate
        )

        return gpxDao.insert(entity)
    }

    override suspend fun updateGpxFileStatus(id: Long, status: GpxUploadStatus) {
        gpxDao.updateStatus(id, status.name, Date().time)
    }

    override fun getPendingGpxFiles(): Flow<List<GpxFile>> {
        return gpxDao.getPendingFiles().map { entities ->
            entities.map { entity ->
                GpxFile(
                    id = entity.id,
                    filePath = entity.filePath,
                    status = GpxUploadStatus.valueOf(entity.status),
                    createdAt = Date(entity.createdAt),
                    lastAttempt = entity.lastAttempt?.let { Date(it) },
                    totalDistance = entity.totalDistance,
                    totalTime = entity.totalTime,
                    avgHeartRate = entity.avgHeartRate,
                    maxHeartRate = entity.maxHeartRate
                )
            }
        }
    }

    override suspend fun uploadGpxFile(file: File): Boolean {
        return try {
            // API 서비스를 통해 파일 업로드
            val requestBody = file.asRequestBody("application/gpx+xml".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("gpx_file", file.name, requestBody)
            val response = gpxApiService.uploadGpxFile(filePart)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getGpxFileById(id: Long): GpxFile? {
        val entity = gpxDao.getGpxFileById(id) ?: return null

        return GpxFile(
            id = entity.id,
            filePath = entity.filePath,
            status = GpxUploadStatus.valueOf(entity.status),
            createdAt = Date(entity.createdAt),
            lastAttempt = entity.lastAttempt?.let { Date(it) },
            totalDistance = entity.totalDistance,
            totalTime = entity.totalTime,
            avgHeartRate = entity.avgHeartRate,
            maxHeartRate = entity.maxHeartRate
        )
    }
}