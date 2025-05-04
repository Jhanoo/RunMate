package com.D107.runmate.domain.usecase.smartInsole

import com.D107.runmate.domain.repository.DataStoreRepository
import javax.inject.Inject

class SaveConnectedInsoleAddressesUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    suspend operator fun invoke(leftAddress: String, rightAddress: String) = dataStoreRepository.saveInsoleAddresses(leftAddress, rightAddress)

}