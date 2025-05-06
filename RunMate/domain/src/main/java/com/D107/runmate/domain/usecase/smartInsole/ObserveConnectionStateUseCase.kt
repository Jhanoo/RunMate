package com.D107.runmate.domain.usecase.smartinsole

import com.D107.runmate.domain.model.smartinsole.InsoleConnectionState
import com.D107.runmate.domain.repository.SmartInsoleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveConnectionStateUseCase @Inject constructor(
    private val smartInsoleRepository: SmartInsoleRepository
) {
    operator fun invoke() : StateFlow<InsoleConnectionState> = smartInsoleRepository.observeConnectionState()

}