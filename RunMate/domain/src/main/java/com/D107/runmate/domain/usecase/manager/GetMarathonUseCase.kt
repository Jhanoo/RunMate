package com.D107.runmate.domain.usecase.manager

import com.D107.runmate.domain.model.manager.MarathonInfo
import com.D107.runmate.domain.repository.manager.MarathonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMarathonsUseCase @Inject constructor(
    private val marathonRepository: MarathonRepository
) {
    suspend operator fun invoke(): Flow<Result<List<MarathonInfo>>> {
        return marathonRepository.getMarathons()
    }
}