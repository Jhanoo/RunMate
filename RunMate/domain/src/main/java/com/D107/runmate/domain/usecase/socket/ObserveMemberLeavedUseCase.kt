package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.model.socket.MemberLeavedData
import com.D107.runmate.domain.repository.socket.SocketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMemberLeavedUseCase @Inject constructor(
    private val repository: SocketRepository
) {
    operator fun invoke(): Flow<MemberLeavedData> = repository.observeMemberLeaved()
}