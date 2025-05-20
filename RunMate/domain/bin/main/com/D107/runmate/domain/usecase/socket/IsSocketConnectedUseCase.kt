package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.repository.socket.SocketRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class IsSocketConnectedUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke(): StateFlow<Boolean> = repository.connectionState()
}