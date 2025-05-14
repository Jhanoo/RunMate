package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.repository.socket.SocketRepository
import javax.inject.Inject

class IsSocketConnectedUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke(): Boolean = repository.isConnected()
}