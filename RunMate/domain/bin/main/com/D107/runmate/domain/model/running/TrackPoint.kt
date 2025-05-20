package com.D107.runmate.domain.model.running

data class TrackPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double?,
    val time: String?,
    val hr: Int?,
    val cadence: Int?,
    val pace: Int?
)