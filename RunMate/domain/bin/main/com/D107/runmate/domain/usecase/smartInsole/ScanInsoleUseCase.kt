package com.D107.runmate.domain.usecase.smartinsole

import com.D107.runmate.domain.model.smartinsole.SmartInsole
import com.D107.runmate.domain.repository.SmartInsoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanInsoleUseCase @Inject constructor(
    private val smartInsoleRepository: SmartInsoleRepository
) {
    operator fun invoke(): Flow<List<SmartInsole>> = smartInsoleRepository.scanInsole()
}