package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.repository.socket.SocketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocationUpdatesUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke(): Flow<MemberLocationData> = repository.observeLocationUpdates()
}