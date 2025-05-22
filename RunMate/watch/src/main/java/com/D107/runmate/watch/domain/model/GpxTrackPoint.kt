package com.D107.runmate.watch.domain.model

import java.util.Date

data class GpxTrackPoint(
    val latitude: Double,   // 위도
    val longitude: Double,  // 경도
    val elevation: Double,  // 고도 (미터)
    val time: Date,         // 시간
    val heartRate: Int,     // 심박수 (BPM)
    val cadence: Int,       // 케이던스
    val pace: Int        // 페이스 (분:초/km)
)

// GPX 파일 메타데이터
data class GpxMetadata(
    val name: String,       // 트랙 이름
    val desc: String = "",  // 설명
    val startTime: Date,    // 시작 시간
    val endTime: Date       // 종료 시간
)

// GPX 파일 상태
enum class GpxUploadStatus {
    WAITING,    // 업로드 대기
    UPLOADING,  // 업로드 중
    SUCCESS,    // 업로드 성공
    FAILED      // 업로드 실패
}

// GPX 파일 정보
data class GpxFile(
    val id: Long = 0,
    val filePath: String,         // 파일 경로
    val status: GpxUploadStatus,  // 업로드 상태
    val createdAt: Date,          // 생성 시간
    val lastAttempt: Date? = null, // 마지막 시도 시간
    val totalDistance: Double,    // 총 거리 (km)
    val totalTime: Long,          // 총 시간 (밀리초)
    val avgHeartRate: Int,        // 평균 심박수
    val maxHeartRate: Int,         // 최대 심박수

    // 추가 필드들
    val avgPace: Double,     // 평균 페이스
    val avgCadence: Int = 0,      // 평균 케이던스
    val startTime: Date = Date(), // 시작 시간
    val endTime: Date = Date(),   // 종료 시간
    val avgElevation: Double = 0.0 // 평균 고도
)