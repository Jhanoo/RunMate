package com.D107.runmate.data.remote.datasource.user

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.user.LoginRequest
import com.D107.runmate.data.remote.request.user.SignupRequest
import com.D107.runmate.data.remote.response.user.LoginResponse
import com.D107.runmate.data.remote.response.user.SignupResponse

interface AuthDataSource {
    suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse>
    suspend fun signup(signupRequest: SignupRequest, profileImageBase64: String? = null): ApiResponse<SignupResponse>
}