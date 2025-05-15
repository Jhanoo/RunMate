package com.D107.runmate.domain.model.running

data class PersonalRunningInfo(
    val distance: Float,
    val avgSpeed: Float,
    val altitude: Double,
    val currentSpeed: Float,
    val altitudeSum: Double,
    val cadence: Int,
    val cadenceSum: Double,
    val currentTime: String
)
