package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.history.HistoryDataSource
import com.D107.runmate.data.remote.response.history.HistoryDetailResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.history.HistoryResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.history.HistoryDetail
import com.D107.runmate.domain.model.history.HistoryInfo
import com.D107.runmate.domain.repository.history.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDataSource: HistoryDataSource
) : HistoryRepository {
    override suspend fun getHistoryList(): Flow<ResponseStatus<HistoryInfo>> {
        return flow {
            when (val response = historyDataSource.getHistoryList()) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "히스토리 전체 조회에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> {
                    emit(ResponseStatus.Success(response.data.toDomainModel()))
                }
            }
        }
    }

    override suspend fun getHistoryDetail(historyId: String): Flow<ResponseStatus<HistoryDetail>> {
        return flow {
            when (val response = historyDataSource.getHistoryDetail(historyId = historyId)) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "히스토리 상세 조회에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> emit(ResponseStatus.Success(response.data.toDomainModel()))
            }
        }
    }

    override suspend fun getHistoryDetailByUserId(
        historyId: String,
        userId: String
    ): Flow<ResponseStatus<HistoryDetail>> {
        return flow {
            when (val response = historyDataSource.getHistoryDetailByUserId(historyId, userId)) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "히스토리 그룹원 상세 조회에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> emit(ResponseStatus.Success(response.data.toDomainModel()))
            }
        }
    }

}