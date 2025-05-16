package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.response.history.HistoryDetailResponse
import com.D107.runmate.data.remote.response.history.HistoryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HistoryService {
    @GET("histories")
    suspend fun getHistoryList(): ApiResponse<HistoryResponse>

    @GET("histories/{historyId}")
    suspend fun getHistoryDetail(@Path("historyId") historyId: String): ApiResponse<HistoryDetailResponse>

    @GET("histories/{historyId}/users/{userId}")
    suspend fun getHistoryDetailByUserId(@Path("historyId") historyId: String, @Path("userId") userId: String): ApiResponse<HistoryDetailResponse>
}