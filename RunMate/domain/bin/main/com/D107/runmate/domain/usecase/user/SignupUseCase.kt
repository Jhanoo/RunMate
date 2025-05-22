package com.D107.runmate.domain.usecase.user

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.user.SignupData
import com.D107.runmate.domain.model.user.UserInfo
import com.D107.runmate.domain.repository.user.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(signupData: SignupData): Flow<ResponseStatus<UserInfo>> {
        return authRepository.signup(signupData)
    }
}