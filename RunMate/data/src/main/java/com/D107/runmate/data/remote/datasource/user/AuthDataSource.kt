package com.D107.runmate.data.remote.datasource.user

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.user.LoginRequest
import com.D107.runmate.data.remote.request.user.SignupRequest
import com.D107.runmate.data.remote.response.user.CheckEmailResponse
import com.D107.runmate.data.remote.response.user.LoginResponse
import com.D107.runmate.data.remote.response.user.SignupResponse
import com.D107.runmate.domain.model.user.ProfileImageSource

interface AuthDataSource {
    suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse>
    suspend fun signup(request: SignupRequest, profileImageSource: ProfileImageSource?): ApiResponse<SignupResponse>
    suspend fun checkEmail(email: String): ApiResponse<Boolean>
}