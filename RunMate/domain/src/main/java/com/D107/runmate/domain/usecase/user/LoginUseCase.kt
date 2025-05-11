package com.D107.runmate.domain.usecase.user

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.user.LoginData
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.user.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStoreRepository: DataStoreRepository
) {
    suspend operator fun invoke(email: String, password: String): Flow<ResponseStatus<LoginData>> {
        return authRepository.login(email, password).onEach { result ->
            if (result is ResponseStatus.Success) {
                // 로그인 성공 시 토큰 저장
                dataStoreRepository.saveAccessToken(result.data.accessToken)
            }
        }
    }
}