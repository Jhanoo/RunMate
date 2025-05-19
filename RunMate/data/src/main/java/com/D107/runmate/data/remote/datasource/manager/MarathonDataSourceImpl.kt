package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.api.MarathonService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.response.manager.MarathonResponse
import javax.inject.Inject

class MarathonDataSourceImpl @Inject constructor(
    private val marathonService: MarathonService
) : MarathonDataSource {
    override suspend fun getMarathons(): ApiResponse<List<MarathonResponse>> {
        return try {
            marathonService.getMarathons()
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

    override suspend fun getMarathonById(marathonId: String): ApiResponse<MarathonResponse> {
        return try {
            marathonService.getMarathonById(marathonId)
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