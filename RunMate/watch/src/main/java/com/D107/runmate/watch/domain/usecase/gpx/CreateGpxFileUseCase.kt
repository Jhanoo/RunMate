package com.D107.runmate.watch.domain.usecase.gpx

import android.util.Log
import com.D107.runmate.watch.domain.model.GpxFile
import com.D107.runmate.watch.domain.model.GpxMetadata
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.D107.runmate.watch.domain.repository.GpxRepository
import java.util.Date
import javax.inject.Inject

class CreateGpxFileUseCase @Inject constructor(
    private val gpxRepository: GpxRepository
) {
    suspend operator fun invoke(
        runName: String,
        totalDistance: Double,
        totalTime: Long,
        avgHeartRate: Int,
        maxHeartRate: Int
    ): Result<Long> {
        Log.d("GPX", "GPX 파일 생성 시작: $runName, 거리=${totalDistance}km, 시간=${totalTime}ms")

        return try {
            // 1. 수집된 트랙 포인트 가져오기
            val trackPoints = gpxRepository.getSessionTrackPoints()
            Log.d("GPX", "조회된 트랙 포인트 수: ${trackPoints.size}")

            // 포인트가 없으면 실패 처리
            if (trackPoints.isEmpty()) {
                Log.e("GPX", "수집된 트랙 포인트가 없습니다")
                return Result.failure(Exception("수집된 트랙 포인트가 없습니다"))
            }

            // 2. 메타데이터 생성
            val metadata = GpxMetadata(
                name = runName,
                desc = "RunMate - ${Date()}",
                startTime = trackPoints.first().time,
                endTime = trackPoints.last().time
            )

            // 3. GPX 파일 생성
            val gpxFile = gpxRepository.createGpxFile(metadata)
            Log.d("GPX", "GPX 파일 생성됨: ${gpxFile.absolutePath}")

            // 4. 파일 정보 DB에 저장
            val gpxFileInfo = GpxFile(
                filePath = gpxFile.absolutePath,
                status = GpxUploadStatus.WAITING,
                createdAt = Date(),
                totalDistance = totalDistance,
                totalTime = totalTime,
                avgHeartRate = avgHeartRate,
                maxHeartRate = maxHeartRate
            )

            val fileId = gpxRepository.saveGpxFileInfo(gpxFileInfo)
            Log.d("GPX", "GPX 파일 정보 저장됨: ID=$fileId")

            // 5. 세션 트랙 포인트 초기화
            gpxRepository.clearSession()

            Result.success(fileId)
        } catch (e: Exception) {
            Log.e("GPX", "GPX 파일 생성 실패: ${e.message}", e)
            Result.failure(e)
        }
    }
}