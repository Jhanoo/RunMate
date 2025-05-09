package com.D107.runmate.watch.domain.usecase.distance

import com.D107.runmate.watch.domain.model.Distance
import com.D107.runmate.watch.domain.repository.DistanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDistanceUseCase @Inject constructor(
    private val distanceRepository: DistanceRepository
) {
    operator fun invoke(): Flow<Distance> = distanceRepository.getDistanceFlow()
}