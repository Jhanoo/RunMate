package com.D107.runmate.data.remote.response

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.running.EndRunning
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EndRunningResponse(
    val historyId: String
): BaseResponse {
    companion object: DataMapper<EndRunningResponse, EndRunning> {
        override fun EndRunningResponse.toDomainModel(): EndRunning {
            return EndRunning(historyId)
        }
    }
}
