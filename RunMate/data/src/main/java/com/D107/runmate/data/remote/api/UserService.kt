package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ServerResponse
import com.D107.runmate.data.remote.request.user.LoginRequest
import com.D107.runmate.data.remote.response.user.LoginResponse
import com.D107.runmate.data.remote.response.user.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserService {
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<ServerResponse<LoginResponse>>

    @Multipart
    @POST("auth/signup")
    suspend fun signup(
        @Part("data") signupRequest: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<ServerResponse<SignupResponse>>
}