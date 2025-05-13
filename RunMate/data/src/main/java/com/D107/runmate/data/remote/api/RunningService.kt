package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiBaseResponse
import com.D107.runmate.data.remote.request.FinishRunningRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RunningService {
    @Multipart
    @POST("ai/runs/end")
    suspend fun endRunning(@Part file: MultipartBody.Part, @Part("request") request: FinishRunningRequest): Response<ApiBaseResponse<Unit>>
}