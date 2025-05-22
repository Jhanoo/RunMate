package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.manager.CurriculumRequest
import com.D107.runmate.data.remote.response.manager.CurriculumCreationResponse
import com.D107.runmate.data.remote.response.manager.CurriculumDetailResponse

interface CurriculumDataSource {
    suspend fun createCurriculum(curriculumRequest: CurriculumRequest): ApiResponse<CurriculumCreationResponse>
    suspend fun getMyCurriculum(): ApiResponse<CurriculumDetailResponse>
}