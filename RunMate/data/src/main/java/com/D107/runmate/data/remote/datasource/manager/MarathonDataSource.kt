package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.response.manager.MarathonResponse

interface MarathonDataSource {
    suspend fun getMarathons(): ApiResponse<List<MarathonResponse>>
    suspend fun getMarathonById(marathonId: String): ApiResponse<MarathonResponse>
}