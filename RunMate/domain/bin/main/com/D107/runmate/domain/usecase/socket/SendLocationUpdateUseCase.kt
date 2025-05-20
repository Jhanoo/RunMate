package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.repository.socket.SocketRepository
import javax.inject.Inject

class SendLocationUpdateUseCase @Inject constructor(private val repository: SocketRepository) {
    operator fun invoke(lat: Double, lng: Double) = repository.sendLocationUpdate(lat, lng)
}
