package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class UserHistoryDetail(
    val avgBpm: Double,
    val avgCadence: Double,
    val avgElevation: Double,
    val avgPace: Double,
    val calories: Double,
    val distance: Double,
    val endTime: String,
    val gpxFile: String,
    val nickname: String,
    val profileImage: String,
    val startTime: String,
    val userId: String
): BaseModel