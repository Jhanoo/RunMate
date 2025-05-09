package com.D107.runmate.domain.usecase.smartinsole

import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.repository.DataStoreRepository
import javax.inject.Inject

class SaveGaitAnalysisResultUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    suspend operator fun invoke(gaitAnalysisResult: GaitAnalysisResult) = dataStoreRepository.saveGaitAnalysisResult(gaitAnalysisResult)

}