package com.D107.runmate.domain.usecase.smartinsole

import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedGaitAnalysisResultUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
){
    operator fun invoke(): Flow<GaitAnalysisResult?> = dataStoreRepository.savedGaitAnalysisResult
}