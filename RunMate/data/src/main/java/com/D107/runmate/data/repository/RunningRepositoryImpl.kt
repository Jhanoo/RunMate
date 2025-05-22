package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.running.RunningDataSource
import com.D107.runmate.data.remote.request.FinishRunningRequest
import com.D107.runmate.data.remote.response.EndRunningResponse.Companion.toDomainModel
import com.D107.runmate.data.utils.GpxWriter
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.running.EndRunning
import com.D107.runmate.domain.repository.running.RunningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

internal class RunningRepositoryImpl @Inject constructor(
    private val runningDataSource: RunningDataSource,
    private val gpxWriter: GpxWriter
) : RunningRepository {
    override suspend fun endRunning(
        avgBpm: Double,
        avgCadence: Double,
        avgElevation: Double,
        avgPace: Double,
        calories: Double,
        courseId: String?,
        distance: Double,
        endTime: String,
        startLocation: String,
        startTime: String,
        groupId:String?
    ): Flow<ResponseStatus<EndRunning>> = flow {
        try {
            when (val response = runningDataSource.endRunning(
                FinishRunningRequest(
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
                    groupId = groupId
                )
            )) {
                is ApiResponse.Success -> {
                    Timber.d("response success: $response")
                    emit(ResponseStatus.Success(response.data!!.toDomainModel()))
                }

                is ApiResponse.Error -> {
                    Timber.d("response error: ${response.error.message}")
                    emit(
                        ResponseStatus.Error(
                            NetworkError(
                                error = response.error.error ?: "RUNNING_ERROR",
                                code = response.error.code ?: "UNKNOWN_CODE",
                                status = response.error.status ?: "ERROR",
                                message = response.error.message ?: "달리기 종료에 실패하였습니다"
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
            gpxWriter.deleteFile()
            emit(
                ResponseStatus.Error(
                    NetworkError(
                        error = "NETWORK_ERROR",
                        code = "CONNECTION_FAILED",
                        status = "FAILED",
                        message = "네트워크 오류가 발생했습니다: ${e.message}"
                    )
                )
            )
        }
    }
}