package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.SocketAuth
import com.D107.runmate.domain.repository.socket.SocketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectSocketUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke(auth: SocketAuth): Flow<ConnectionStatus> = repository.connect(auth)
}