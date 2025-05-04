package com.D107.runmate.domain.usecase.smartInsole

import com.D107.runmate.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedInsoleAddressesUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    operator fun invoke(): Flow<Pair<String?, String?>> = dataStoreRepository.savedInsoleAddresses

}