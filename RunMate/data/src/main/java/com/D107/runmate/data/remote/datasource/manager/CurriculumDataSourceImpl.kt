package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.api.CurriculumService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.request.manager.CurriculumRequest
import com.D107.runmate.data.remote.response.manager.CurriculumCreationResponse
import com.D107.runmate.data.remote.response.manager.CurriculumDetailResponse
import javax.inject.Inject

class CurriculumDataSourceImpl @Inject constructor(
    private val curriculumService: CurriculumService
) : CurriculumDataSource {
    override suspend fun createCurriculum(curriculumRequest: CurriculumRequest): ApiResponse<CurriculumCreationResponse> {
        return try {
            curriculumService.createCurriculum(curriculumRequest)
        } catch (e: Exception) {
            ApiResponse.Error(
                ErrorResponse(
                    status = "NETWORK_ERROR",
                    error = "CONNECTION_FAILED",
                    code = "NETWORK_ERROR",
                    message = "서버에 연결할 수 없습니다: ${e.message}"
                )
            )
        }
    }

    override suspend fun getMyCurriculum(): ApiResponse<CurriculumDetailResponse> {
        return try {
            curriculumService.getMyCurriculum()
        } catch (e: Exception) {
            ApiResponse.Error(
                ErrorResponse(
                    status = "NETWORK_ERROR",
                    error = "CONNECTION_FAILED",
                    code = "NETWORK_ERROR",
                    message = "서버에 연결할 수 없습니다: ${e.message}"
                )
            )
        }
    }
}