package com.D107.runmate.domain.usecase

import com.D107.runmate.domain.repository.DataStoreRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {

}