package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.manager.CurriculumRequest
import com.D107.runmate.data.remote.response.manager.CurriculumCreationResponse
import com.D107.runmate.data.remote.response.manager.CurriculumDetailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CurriculumService {
    @POST("curricula/create")
    suspend fun createCurriculum(
        @Body createCurriculumRequest: CurriculumRequest
    ): ApiResponse<CurriculumCreationResponse>

    @GET("curricula/my")
    suspend fun getMyCurriculum(): ApiResponse<CurriculumDetailResponse>
}