package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.api.MarathonService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.response.manager.MarathonResponse
import javax.inject.Inject

class MarathonDataSourceImpl @Inject constructor(
    private val marathonService: MarathonService,
    private val handler: ApiResponseHandler
) : MarathonDataSource {
    override suspend fun getMarathons(): ApiResponse<List<MarathonResponse>> {
        return handler.handle {
            marathonService.getMarathons()
        }
    }

    override suspend fun getMarathonById(marathonId: String): ApiResponse<MarathonResponse> {
        return handler.handle {
            marathonService.getMarathonById(marathonId)
        }
    }
}