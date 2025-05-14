package com.D107.runmate.data.remote.datasource.running

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.FinishRunningRequest

interface RunningDataSource {
    suspend fun endRunning(request: FinishRunningRequest): ApiResponse<Any>
}