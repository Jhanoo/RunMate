package com.D107.runmate.data.remote.datasource.running

import android.content.Context
import com.D107.runmate.data.remote.api.RunningService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.request.FinishRunningRequest
import com.D107.runmate.data.remote.response.EndRunningResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class RunningDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val runningService: RunningService,
    private val handler: ApiResponseHandler
): RunningDataSource {
    override suspend fun endRunning(request: FinishRunningRequest): ApiResponse<EndRunningResponse> {
        return handler.handle {
            val fileName = "running_tracking.gpx"
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                ApiResponse.Error(
                    ErrorResponse(
                        status = "400",
                        error = "FILE_NOT_FOUND",
                        code = "FILE_NOT_FOUND",
                        message = "트래킹 파일이 존재하지 않습니다."
                    )
                )
            }
            Timber.d("request ${request}")
            val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("gpxFile", file.name, requestBody)
            runningService.endRunning(filePart, request)
        }
    }
}