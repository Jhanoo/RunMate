package com.D107.runmate.domain.usecase.smartinsole

import com.D107.runmate.domain.repository.SmartInsoleRepository
import javax.inject.Inject

class DisconnectInsoleUseCase @Inject constructor(
    private val smartInsoleRepository: SmartInsoleRepository
) {
    operator fun invoke() = smartInsoleRepository.disconnect()

}