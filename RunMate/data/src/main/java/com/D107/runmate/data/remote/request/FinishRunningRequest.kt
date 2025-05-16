package com.D107.runmate.data.remote.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FinishRunningRequest(
    val avgBpm: Double,
    val avgCadence: Double,
    val avgElevation: Double,
    val avgPace: Double,
    val calories: Double,
    val courseId: String?,
    val distance: Double,
    val endTime: String,
    val startLocation: String,
    val startTime: String
)