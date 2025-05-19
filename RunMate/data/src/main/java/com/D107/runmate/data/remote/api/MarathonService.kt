package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ServerResponse
import com.D107.runmate.data.remote.response.manager.MarathonResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MarathonService {
    @GET("marathons")
    suspend fun getMarathons(): Response<ServerResponse<List<MarathonResponse>>>

    @GET("marathons/{marathonId}")
    suspend fun getMarathonById(@Path("marathonId") marathonId: String): Response<ServerResponse<MarathonResponse>>
}