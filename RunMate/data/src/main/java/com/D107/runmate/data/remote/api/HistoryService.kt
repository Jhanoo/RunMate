package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ServerResponse
import com.D107.runmate.data.remote.response.history.HistoryDetailResponse
import com.D107.runmate.data.remote.response.history.HistoryResponse
import com.D107.runmate.data.remote.response.history.UserHistoryDetailResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoryService {
    @GET("histories")
    suspend fun getHistoryList(@Query("size") size: Int): Response<ServerResponse<HistoryResponse>>

    @GET("histories/{historyId}")
    suspend fun getHistoryDetail(@Path("historyId") historyId: String): Response<ServerResponse<HistoryDetailResponse>>

    @GET("histories/{historyId}/users/{userId}")
    suspend fun getHistoryDetailByUserId(@Path("historyId") historyId: String, @Path("userId") userId: String): Response<ServerResponse<UserHistoryDetailResponse>>
}