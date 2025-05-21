package com.D107.runmate.domain.usecase.history

import com.D107.runmate.domain.repository.history.HistoryRepository
import javax.inject.Inject

class GetUserHistoryDetailUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(historyId: String, userId: String) = historyRepository.getHistoryDetailByUserId(historyId, userId)
}