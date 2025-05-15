package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.response.manager.MarathonResponse
import retrofit2.http.GET

interface MarathonService {
    @GET("api/marathons")
    suspend fun getMarathons(): ApiResponse<List<MarathonResponse>>
}