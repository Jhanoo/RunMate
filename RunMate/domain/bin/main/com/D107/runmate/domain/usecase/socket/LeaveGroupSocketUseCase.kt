package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.repository.socket.SocketRepository
import javax.inject.Inject

class LeaveGroupSocketUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke() = repository.leaveGroup()
}