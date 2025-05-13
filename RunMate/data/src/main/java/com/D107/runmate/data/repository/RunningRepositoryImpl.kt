package com.D107.runmate.data.repository

import android.content.Context
import com.D107.runmate.data.local.UserDataStoreSource
import com.D107.runmate.data.remote.api.RunningService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.common.ErrorResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.request.FinishRunningRequest
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.repository.running.RunningRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

internal class RunningRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val runningService: RunningService,
    private val dataStore: UserDataStoreSource
):RunningRepository {
    override suspend fun endRunning(
        avgBpm: Double,
        avgCadence: Double,
        avgElevation: Double,
        avgPace: Double,
        calories: Double,
        courseId: String,
        distance: Double,
        endTime: String,
        startLocation: String,
        startTime: String
    ): Flow<ResponseStatus<Unit>> {
        return flow {
            ApiResponseHandler().handle {
                val fileName = "running_tracking.gpx"
                val file = File(context.filesDir, fileName)
                if (!file.exists()) {
                    emit(ResponseStatus.Error(NetworkError(code = "FILE_NOT_FOUND", message = "File not found: $fileName")))
                }

                // 2. 파일을 네트워크 전송용 MultipartBody.Part로 변환
                val requestBody = file.asRequestBody("application/xml".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("gpxFile", file.name, requestBody)

                val userId = dataStore.userId.first() ?: "-1"
                runningService.endRunning(
                    filePart
                    ,FinishRunningRequest(
                        avgBpm = avgBpm,
                        avgCadence = avgCadence,
                        avgElevation = avgElevation,
                        avgPace = avgPace,
                        calories = calories,
                        courseId = courseId,
                        distance = distance,
                        endTime = endTime,
                        startLocation = startLocation,
                        startTime = startTime,
                    )
                )
            }.onEach { result ->
                when(result) {
                    is ApiResponse.Success -> {
                        val directory = context.filesDir
                        try{
                            File(directory, "running_tracking.gpx").delete()
                        }catch (e: Exception) {
                            Timber.d("file delete error ${e.message}")
                            emit(ResponseStatus.Error(NetworkError(code = "DELETE_ERROR", message = e.message ?: "File not found ")))
                        }
                        emit(ResponseStatus.Success(result.data))
                    }
                    is ApiResponse.Error -> {
                        emit(ResponseStatus.Error(result.error.toDomainModel()))
                    }
                }
            }
        }
    }
}