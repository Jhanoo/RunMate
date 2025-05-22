package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.history.UserHistoryDetail

data class UserHistoryDetailResponse(
    val avgBpm: Double,
    val avgCadence: Double,
    val avgElevation: Double,
    val avgPace: Double,
    val calories: Double,
    val distance: Double,
    val endTime: String,
    val gpxFile: String,
    val nickname: String,
    val profileImage: String?,
    val startTime: String,
    val userId: String
):BaseResponse {
    companion object: DataMapper<UserHistoryDetailResponse, UserHistoryDetail> {
        override fun UserHistoryDetailResponse.toDomainModel(): UserHistoryDetail {
            return UserHistoryDetail(avgBpm, avgCadence, avgElevation, avgPace, calories, distance, endTime, gpxFile, nickname, profileImage, startTime, userId)
        }
    }
}