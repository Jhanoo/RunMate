package com.D107.runmate.watch.data.repository

import android.content.Context
import android.util.Log
import com.D107.runmate.watch.data.local.GpxDao
import com.D107.runmate.watch.data.local.GpxEntity
import com.D107.runmate.watch.data.local.TrackPointEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gpxDao: GpxDao
) : GpxRepository {

    // 메모리 버퍼 크기 제한 설정
    private val MAX_BUFFER_SIZE = 120 // 10분 간격으로 DB에 저장

    // 현재 세션 ID
    private var currentSessionId: String = UUID.randomUUID().toString()

    // 현재 세션의 트랙 포인트 저장소 (메모리)
    private val sessionTrackPoints = mutableListOf<GpxTrackPoint>()

    override suspend fun addTrackPoint(trackPoint: GpxTrackPoint) {
        sessionTrackPoints.add(trackPoint)
        Log.d("GpxTracking", "트랙 포인트 추가됨: 현재 총 ${sessionTrackPoints.size}개 포인트")

        // 버퍼가 꽉 차면 DB에 일괄 저장하고 메모리에서 제거
        if (sessionTrackPoints.size >= MAX_BUFFER_SIZE) {
            saveTrackPointsBatch(sessionTrackPoints)
            sessionTrackPoints.clear()
        }
    }

    override suspend fun getSessionTrackPoints(): List<GpxTrackPoint> {
        // 현재 세션의 모든 트랙 포인트를 가져옴 (메모리 + DB)
        return getAllTrackPoints(currentSessionId)
    }

    override suspend fun clearSession() {
        // 메모리 포인트 삭제
        val memoryCount = sessionTrackPoints.size
        sessionTrackPoints.clear()

        // DB 포인트도 삭제
        val dbCount = gpxDao.deleteTrackPointsBySession(currentSessionId)

        Log.d("GpxTracking", "세션 트랙 포인트 초기화: 메모리 ${memoryCount}개, DB ${dbCount}개 삭제됨")

        // 새 세션 ID 생성
        currentSessionId = UUID.randomUUID().toString()
    }

    override suspend fun createGpxFile(metadata: GpxMetadata): File {
        // 모든 트랙 포인트 로드 (메모리 + DB)
        val allTrackPoints = getSessionTrackPoints()
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
            trackPoints = allTrackPoints,
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
            maxHeartRate = gpxFile.maxHeartRate,
            avgPace = gpxFile.avgPace,
            avgCadence = gpxFile.avgCadence,
            avgElevation = gpxFile.avgElevation,
            startTime = gpxFile.startTime.time,
            endTime = gpxFile.endTime.time
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
                    maxHeartRate = entity.maxHeartRate,
                    avgPace = entity.avgPace,
                    avgCadence = entity.avgCadence,
                    avgElevation = entity.avgElevation,
                    startTime = Date(entity.startTime),
                    endTime = Date(entity.endTime)
                )
            }
        }
    }

    override suspend fun uploadGpxFile(file: File): Boolean {
        return true
        //        return try {
//            // API 서비스를 통해 파일 업로드
//            val requestBody = file.asRequestBody("application/gpx+xml".toMediaTypeOrNull())
//            val filePart = MultipartBody.Part.createFormData("gpx_file", file.name, requestBody)
//            val response = gpxApiService.uploadGpxFile(filePart)
//            response.isSuccessful
//        } catch (e: Exception) {
//            false
//        }
    }

    override suspend fun getGpxFileById(id: Long): GpxFile? {
        val entity = gpxDao.getGpxFileById(id) ?: return null

        val result = GpxFile(
            id = entity.id,
            filePath = entity.filePath,
            status = GpxUploadStatus.valueOf(entity.status),
            createdAt = Date(entity.createdAt),
            lastAttempt = entity.lastAttempt?.let { Date(it) },
            totalDistance = entity.totalDistance,
            totalTime = entity.totalTime,
            avgHeartRate = entity.avgHeartRate,
            maxHeartRate = entity.maxHeartRate,
            avgPace = entity.avgPace,
            avgCadence = entity.avgCadence,
            startTime = Date(entity.startTime),
            endTime = Date(entity.endTime),
            avgElevation = entity.avgElevation
        )

        Log.d("GPX", "최근 GPX 파일 정보: ID=${result.id}, 경로=${result.filePath}")
        Log.d("GPX", "러닝 통계: 거리=${result.totalDistance}km, 시간=${result.totalTime}ms, " +
                "평균 심박수=${result.avgHeartRate}bpm, 최대 심박수=${result.maxHeartRate}bpm")
        Log.d("GPX", "추가 정보: 평균 페이스=${result.avgPace}, 평균 케이던스=${result.avgCadence}, " +
                "시작 시간=${formatDate(result.startTime)}, 종료 시간=${formatDate(result.endTime)}, " +
                "평균 고도=${result.avgElevation}m")

        return result
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
    }

    override suspend fun saveTrackPointsBatch(trackPoints: List<GpxTrackPoint>) {
        // TrackPoint 엔티티로 변환
        val entities = trackPoints.map { point ->
            TrackPointEntity(
                sessionId = currentSessionId,
                latitude = point.latitude,
                longitude = point.longitude,
                elevation = point.elevation,
                time = point.time.time,
                heartRate = point.heartRate,
                cadence = point.cadence,
                pace = point.pace
            )
        }

        // DB에 일괄 저장
        gpxDao.insertTrackPoints(entities)
        Log.d("GpxTracking", "${entities.size}개의 트랙 포인트를 DB에 저장")
    }

    override suspend fun getAllTrackPoints(sessionId: String): List<GpxTrackPoint> {
        // 1. 메모리에 있는 포인트 먼저 수집
        val memoryPoints = sessionTrackPoints.toList()

        // 2. DB에 저장된 포인트도 가져옴
        val dbPoints = gpxDao.getTrackPointsBySession(sessionId).map { entity ->
            GpxTrackPoint(
                latitude = entity.latitude,
                longitude = entity.longitude,
                elevation = entity.elevation,
                time = Date(entity.time),
                heartRate = entity.heartRate,
                cadence = entity.cadence,
                pace = entity.pace
            )
        }

        // 3. 두 목록을 합쳐서 시간순으로 정렬
        val allPoints = (memoryPoints + dbPoints).sortedBy { it.time }
        Log.d(
            "GpxTracking",
            "총 ${allPoints.size}개의 ㄴ트랙 포인트 로드 (메모리: ${memoryPoints.size}, DB: ${dbPoints.size})"
        )

        return allPoints
    }
}