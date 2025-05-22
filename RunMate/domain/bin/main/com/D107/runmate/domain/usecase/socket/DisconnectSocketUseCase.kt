package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.repository.socket.SocketRepository
import javax.inject.Inject

class DisconnectSocketUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke() = repository.disconnect()
}
