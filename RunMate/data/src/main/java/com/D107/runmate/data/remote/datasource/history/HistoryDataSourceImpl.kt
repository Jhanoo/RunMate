package com.D107.runmate.data.remote.datasource.history

import com.D107.runmate.data.remote.api.HistoryService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.response.history.HistoryDetailResponse
import com.D107.runmate.data.remote.response.history.HistoryResponse
import com.D107.runmate.data.remote.response.history.UserHistoryDetailResponse
import javax.inject.Inject

class HistoryDataSourceImpl @Inject constructor(
    private val historyService: HistoryService,
    private val handler: ApiResponseHandler
): HistoryDataSource {
    override suspend fun getHistoryList(): ApiResponse<HistoryResponse> {
        return handler.handle {
            historyService.getHistoryList(100)
        }
    }

    override suspend fun getHistoryDetail(historyId: String): ApiResponse<HistoryDetailResponse> {
        return handler.handle {
            historyService.getHistoryDetail(historyId)
        }
    }

    override suspend fun getHistoryDetailByUserId(
        historyId: String,
        userId: String
    ): ApiResponse<UserHistoryDetailResponse> {
        return handler.handle {
            historyService.getHistoryDetailByUserId(historyId, userId)
        }
    }
}