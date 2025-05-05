package com.D107.runmate.watch.domain.usecase.distance

import com.D107.runmate.watch.domain.repository.DistanceRepository
import javax.inject.Inject

class StopDistanceMonitoringUseCase @Inject constructor(
    private val distanceRepository: DistanceRepository
) {
    suspend operator fun invoke() = distanceRepository.stopDistanceMonitoring()
}