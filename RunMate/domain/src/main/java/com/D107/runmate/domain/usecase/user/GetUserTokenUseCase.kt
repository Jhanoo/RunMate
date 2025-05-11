package com.D107.runmate.domain.usecase.user

import com.D107.runmate.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserTokenUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    operator fun invoke(): Flow<String?> {
        return dataStoreRepository.accessToken
    }
}