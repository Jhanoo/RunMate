package com.D107.runmate.watch.domain.usecase.gpx

import android.util.Log
import com.D107.runmate.watch.domain.model.GpxFile
import com.D107.runmate.watch.domain.model.GpxMetadata
import com.D107.runmate.watch.domain.model.GpxUploadStatus
import com.D107.runmate.watch.domain.repository.GpxRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CreateGpxFileUseCase @Inject constructor(
    private val gpxRepository: GpxRepository
) {
    suspend operator fun invoke(
        runName: String,
        totalDistance: Double,
        totalTime: Long,
        avgHeartRate: Int,
        maxHeartRate: Int,
        avgPace: Double,
        avgCadence: Int,
        startTime: Date,
        endTime: Date,
        avgElevation: Double
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

            // 시작 및 종료 시간 계산
            val startTime = trackPoints.minByOrNull { it.time }?.time ?: Date()
            val endTime = trackPoints.maxByOrNull { it.time }?.time ?: Date()

            // 평균 고도 계산
            val avgElevation = trackPoints.map { it.elevation }.average()

            // 유효한 페이스 값만 추출하여 평균 계산
            val validPaces = trackPoints
                .map { it.pace }
                .filter {
                    it > 0
                }

            val avgPaceSeconds = if (validPaces.isNotEmpty()) {
                validPaces.average()
            } else {
                0.0
            }

            // 유효한 케이던스 값만 추출하여 평균 계산
            val validCadences = trackPoints
                .map { it.cadence }
                .filter { it > 0 }

            val avgCadence = if (validCadences.isNotEmpty()) {
                validCadences.sum() / validCadences.size
            } else {
                0
            }
            
            // 2. 메타데이터 생성
            val metadata = GpxMetadata(
                name = runName,
                desc = "RunMate - ${Date()}",
                startTime = startTime,
                endTime = endTime
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
                maxHeartRate = maxHeartRate,
                avgPace = avgPaceSeconds,
                avgCadence = avgCadence,
                startTime = startTime,
                endTime = endTime,
                avgElevation = avgElevation
            )

            val fileId = gpxRepository.saveGpxFileInfo(gpxFileInfo)

            val formattedTime = formatTime(totalTime)

            // 저장된 정보 로그
            Log.d("GPX", "저장된 러닝 데이터: ID=$fileId, 경로=${gpxFile.absolutePath}")
            Log.d("GPX", "러닝 통계: 거리=${totalDistance}km, 시간=${formattedTime}, " +
                    "평균 심박수=${avgHeartRate}bpm, 최대 심박수=${maxHeartRate}bpm")
            Log.d("GPX", "추가 정보: 평균 페이스=${avgPace}, 평균 케이던스=${avgCadence}, " +
                    "시작 시간=${formatDate(startTime)}, 종료 시간=${formatDate(endTime)}, " +
                    "평균 고도=${avgElevation}m")

            // 5. 세션 트랙 포인트 초기화
            gpxRepository.clearSession()

            Result.success(fileId)
        } catch (e: Exception) {
            Log.e("GPX", "GPX 파일 생성 실패: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 페이스 문자열을 초로 변환
//    private fun paceToSeconds(pace: String): Long {
//        try {
//            val parts = pace.split("'", "\"")
//            if (parts.size >= 2) {
//                val minutes = parts[0].toLong()
//                val seconds = parts[1].toLong()
//                return minutes * 60 + seconds
//            }
//        } catch (e: Exception) {
//            Log.e("GPX", "페이스 변환 실패: $pace", e)
//        }
//        return 0
//    }

    // 초를 페이스 문자열로 변환
    private fun secondsToPace(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "0'00\""
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes'${seconds.toString().padStart(2, '0')}\""
    }

    private fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / (1000 * 60)) % 60
        val hours = (timeMs / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
    }
}