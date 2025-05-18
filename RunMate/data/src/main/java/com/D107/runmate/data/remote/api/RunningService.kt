package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.FinishRunningRequest
import com.D107.runmate.data.remote.response.EndRunningResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RunningService {
    @Multipart
    @POST("runs/end")
    suspend fun endRunning(@Part file: MultipartBody.Part?, @Part("request") request: FinishRunningRequest?): ApiResponse<EndRunningResponse>
}