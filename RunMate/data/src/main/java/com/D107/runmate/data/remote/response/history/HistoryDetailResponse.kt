package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.history.GroupRunItem.Companion.toDomainModel
import com.D107.runmate.data.remote.response.history.HistoryItem.Companion.toDomainModel
import com.D107.runmate.data.remote.response.history.MyRunItem.Companion.toDomainModel
import com.D107.runmate.domain.model.history.HistoryDetail
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryDetailResponse(
    val gpxFile: String,
    val groupId: String?,
    @Json(name = "groupRun") val groupRunItem: List<GroupRunItem?>,
    val historyId: String,
    @Json(name = "myRun") val myRunItem: MyRunItem,
    val startLocation: String
): BaseResponse {
    companion object: DataMapper<HistoryDetailResponse, HistoryDetail> {
        override fun HistoryDetailResponse.toDomainModel(): HistoryDetail {
            return HistoryDetail(gpxFile, groupId, if(groupRunItem.isEmpty()) emptyList() else this.groupRunItem.map { it!!.toDomainModel() } , historyId, myRunItem.toDomainModel(), startLocation)
        }
    }
}