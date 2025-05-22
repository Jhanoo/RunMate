package com.D107.runmate.watch.domain.usecase.heartRate

import com.D107.runmate.watch.domain.repository.HeartRateRepository
import javax.inject.Inject

class StopHeartRateMonitoringUseCase @Inject constructor(
    private val heartRateRepository: HeartRateRepository
) {
    suspend operator fun invoke() = heartRateRepository.stopHeartRateMonitoring()
}