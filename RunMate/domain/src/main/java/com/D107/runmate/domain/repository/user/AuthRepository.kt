package com.D107.runmate.domain.repository.user

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.user.LoginData
import com.D107.runmate.domain.model.user.SignupData
import com.D107.runmate.domain.model.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String, fcmToken: String?): Flow<ResponseStatus<LoginData>>
    suspend fun signup(signupData: SignupData): Flow<ResponseStatus<UserInfo>>
    suspend fun checkEmail(email: String): Flow<ResponseStatus<Boolean>>
}