package com.D107.runmate.data.repository

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.user.AuthDataSource
import com.D107.runmate.data.remote.request.user.LoginRequest
import com.D107.runmate.data.remote.request.user.SignupRequest
import com.D107.runmate.data.remote.response.user.LoginResponse
import com.D107.runmate.data.remote.response.user.LoginResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.user.SignupResponse
import com.D107.runmate.data.remote.response.user.SignupResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.user.LoginData
import com.D107.runmate.domain.model.user.SignupData
import com.D107.runmate.domain.model.user.UserInfo
import com.D107.runmate.domain.repository.user.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Flow<ResponseStatus<LoginData>> = flow {
        val request = LoginRequest(email, password)
        when (val response = authDataSource.login(request)) {
            is ApiResponse.Success -> {
                emit(ResponseStatus.Success(response.data.toDomainModel()))
            }
            is ApiResponse.Error -> {
                emit(ResponseStatus.Error(
                    NetworkError(
                        error = response.error.error ?: "LOGIN_ERROR",
                        code = response.error.code ?: "UNKNOWN_CODE",
                        status = response.error.status ?: "ERROR",
                        message = response.error.message ?: "로그인에 실패했습니다."
                    )
                ))
            }
        }
    }

    override suspend fun signup(signupData: SignupData): Flow<ResponseStatus<UserInfo>> = flow {
        val request = SignupRequest(
            email = signupData.email,
            password = signupData.password,
            nickname = signupData.nickname,
            birthday = signupData.birthday,
            gender = signupData.gender
        )

        try {
            when (val response = authDataSource.signup(request, signupData.profileImageSource)) {
                is ApiResponse.Success -> {
                    emit(ResponseStatus.Success(response.data.toDomainModel()))
                }
                is ApiResponse.Error -> {
                    emit(ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "SIGNUP_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "회원가입에 실패했습니다."
                        )
                    ))
                }
            }
        } catch (e: Exception) {
            emit(ResponseStatus.Error(
                NetworkError(
                    error = "NETWORK_ERROR",
                    code = "CONNECTION_FAILED",
                    status = "FAILED",
                    message = "네트워크 오류가 발생했습니다: ${e.message}"
                )
            ))
        }
    }
}