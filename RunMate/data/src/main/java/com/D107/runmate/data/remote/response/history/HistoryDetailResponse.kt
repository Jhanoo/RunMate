package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.history.GroupRunItem.Companion.toDomainModel
import com.D107.runmate.data.remote.response.history.HistoryItem.Companion.toDomainModel
import com.D107.runmate.data.remote.response.history.MyRunItem.Companion.toDomainModel
import com.D107.runmate.domain.model.history.HistoryDetail
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryDetailResponse(
    val gpxFile: String,
    val groupRunItem: List<GroupRunItem>,
    val historyId: String,
    val myRunItem: MyRunItem
): BaseResponse {
    companion object: DataMapper<HistoryDetailResponse, HistoryDetail> {
        override fun HistoryDetailResponse.toDomainModel(): HistoryDetail {
            return HistoryDetail(gpxFile, groupRunItem.map { it.toDomainModel() }, historyId, myRunItem.toDomainModel())
        }
    }
}