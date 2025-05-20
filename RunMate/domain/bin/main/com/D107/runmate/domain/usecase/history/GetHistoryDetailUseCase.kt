package com.D107.runmate.domain.usecase.history

import com.D107.runmate.domain.repository.history.HistoryRepository
import javax.inject.Inject

class GetHistoryDetailUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
){
    suspend operator fun invoke(historyId: String) = historyRepository.getHistoryDetail(historyId)
}