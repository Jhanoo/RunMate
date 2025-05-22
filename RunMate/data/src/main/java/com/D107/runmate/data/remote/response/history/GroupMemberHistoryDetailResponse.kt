package com.D107.runmate.data.remote.response.history

data class GroupMemberHistoryDetailResponse(
    val avgBpm: Int,
    val avgCadence: Int,
    val avgElevation: Double,
    val avgPace: Int,
    val calories: Int,
    val distance: Int,
    val endTime: String,
    val gpxFile: String,
    val nickname: String,
    val profileImage: String?,
    val startTime: String,
    val userId: String
)