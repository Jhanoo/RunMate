package com.D107.runmate.watch.domain.usecase.cadence

import com.D107.runmate.watch.domain.repository.CadenceRepository
import javax.inject.Inject

class GetCadenceUseCase @Inject constructor(
    private val cadenceRepository: CadenceRepository
) {
    operator fun invoke() = cadenceRepository.getCurrentCadence()
}