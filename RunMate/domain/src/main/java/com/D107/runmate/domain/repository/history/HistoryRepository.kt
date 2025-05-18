package com.D107.runmate.domain.repository.history

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.history.HistoryDetail
import com.D107.runmate.domain.model.history.HistoryInfo
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun getHistoryList(): Flow<ResponseStatus<HistoryInfo>>
    suspend fun getHistoryDetail(historyId: String): Flow<ResponseStatus<HistoryDetail>>
    suspend fun getHistoryDetailByUserId(historyId: String, userId: String): Flow<ResponseStatus<HistoryDetail>>
}