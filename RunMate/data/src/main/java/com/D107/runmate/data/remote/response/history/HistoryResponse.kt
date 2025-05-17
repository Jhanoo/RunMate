package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.history.HistoryItem.Companion.toDomainModel
import com.D107.runmate.domain.model.history.HistoryInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryResponse(
    @Json(name="histories") val histories: List<HistoryItem>,
    val page: Int,
    val size: Int,
    val total: Int
): BaseResponse {
    companion object: DataMapper<HistoryResponse, HistoryInfo> {
        override fun HistoryResponse.toDomainModel(): HistoryInfo {
            return HistoryInfo(histories.map { it.toDomainModel() }, page, size, total)
        }
    }
}