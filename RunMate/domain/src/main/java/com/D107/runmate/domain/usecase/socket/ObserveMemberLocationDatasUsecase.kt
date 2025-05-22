package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.repository.socket.SocketRepository
import javax.inject.Inject

class ObserveMemberLocationDatasUsecase @Inject constructor(
    private val socketRepository: SocketRepository
) {
    operator fun invoke() = socketRepository.memberLocationDatas
}