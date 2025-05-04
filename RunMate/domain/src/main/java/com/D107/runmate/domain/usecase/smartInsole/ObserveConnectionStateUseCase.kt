package com.D107.runmate.domain.usecase.smartInsole

import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.repository.SmartInsoleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveConnectionStateUseCase @Inject constructor(
    private val smartInsoleRepository: SmartInsoleRepository
) {
    operator fun invoke() : StateFlow<InsoleConnectionState> = smartInsoleRepository.observeConnectionState()

}