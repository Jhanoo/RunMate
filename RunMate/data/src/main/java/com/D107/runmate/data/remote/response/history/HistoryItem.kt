package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.history.History
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryItem(
    val courseName: String?,
    val duration: Int,
    val groupName: String?,
    val historyId: String,
    val location: String,
    val members: List<String>,
    val myDistance: Double,
    val startTime: String
): BaseResponse {
    companion object: DataMapper<HistoryItem, History> {
        override fun HistoryItem.toDomainModel(): History {
            return History(courseName, duration, groupName, historyId, location, members, myDistance, startTime)
        }
    }
}