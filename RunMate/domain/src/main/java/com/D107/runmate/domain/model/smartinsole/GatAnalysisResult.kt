package com.D107.runmate.domain.model.smartinsole

import kotlinx.serialization.Serializable

@Serializable
data class GaitAnalysisResult(
    //왼발
    val dominantLeftFootStrike: FootStrikeType = FootStrikeType.UNKNOWN,
    val leftFootStrikeDistribution: Map<FootStrikeType, Int> = emptyMap(),
    val averageLeftYaw: Float? = null,
    val totalLeftSteps: Int = 0,

    //오른발
    val dominantRightFootStrike: FootStrikeType = FootStrikeType.UNKNOWN,
    val rightFootStrikeDistribution: Map<FootStrikeType, Int> = emptyMap(),
    val averageRightYaw: Float? = null,
    val totalRightSteps: Int = 0,
    val analysisDurationMs: Long? = null,

    val gaitPatternDistribution: Map<GaitPatternType,Int>,

    val overallGaitPattern: GaitPatternType = GaitPatternType.UNKNOWN,
    val timestamp:Long = System.currentTimeMillis()
)

@Serializable
enum class FootStrikeType {
    REARFOOT,   // 뒷꿈치 착지
    MIDFOOT,    // 발 중간 착지
    FOREFOOT,   // 앞꿈치 착지
    UNKNOWN     // 판별 불가 또는 데이터 부족
}

@Serializable
enum class GaitPatternType {
    IN_TOEING,  // 안짱 걸음
    OUT_TOEING, // 팔자 걸음
    NEUTRAL,    // 정상 걸음
    UNKNOWN     // 판별 불가 또는 데이터 부족
}