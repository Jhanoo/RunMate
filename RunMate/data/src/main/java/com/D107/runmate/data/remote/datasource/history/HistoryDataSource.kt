package com.D107.runmate.data.remote.datasource.history

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.response.history.HistoryDetailResponse
import com.D107.runmate.data.remote.response.history.HistoryResponse

interface HistoryDataSource {
    suspend fun getHistoryList(): ApiResponse<HistoryResponse>
    suspend fun getHistoryDetail(historyId: String): ApiResponse<HistoryDetailResponse>
    suspend fun getHistoryDetailByUserId(historyId: String, userId: String): ApiResponse<HistoryDetailResponse>
}