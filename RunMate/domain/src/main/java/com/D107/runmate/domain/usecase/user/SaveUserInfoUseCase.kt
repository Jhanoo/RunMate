package com.D107.runmate.domain.usecase.user

import com.D107.runmate.domain.model.user.UserInfo
import com.D107.runmate.domain.repository.DataStoreRepository
import javax.inject.Inject

class SaveUserInfoUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) {
    suspend operator fun invoke(userInfo: UserInfo) {
        dataStoreRepository.saveUserId(userInfo.userId.toLong())
        dataStoreRepository.saveNickname(userInfo.nickname)
    }
}