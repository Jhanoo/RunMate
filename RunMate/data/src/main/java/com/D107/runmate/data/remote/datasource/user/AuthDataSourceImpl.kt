package com.D107.runmate.data.remote.datasource.user

import com.D107.runmate.data.remote.api.UserService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.request.user.LoginRequest
import com.D107.runmate.data.remote.request.user.SignupRequest
import com.D107.runmate.data.remote.response.user.LoginResponse
import com.D107.runmate.data.remote.response.user.SignupResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AuthDataSourceImpl @Inject constructor(
    private val userService: UserService,
    private val moshi: Moshi
) : AuthDataSource {
    override suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse> {
        return try {
            userService.login(loginRequest)
        } catch (e: Exception) {
            // 네트워크 예외 처리
            ApiResponse.Error(
                ErrorResponse(
                    status = "NETWORK_ERROR",
                    error = "CONNECTION_FAILED",
                    code = "NETWORK_ERROR",
                    message = "서버에 연결할 수 없습니다: ${e.message}"
                )
            )
        }
    }

    override suspend fun signup(signupRequest: SignupRequest, profileImageBase64: String?): ApiResponse<SignupResponse> {
        return try {
            // SignupRequest 객체를 JSON 문자열로 변환
            val jsonAdapter: JsonAdapter<SignupRequest> = moshi.adapter(SignupRequest::class.java)
            val jsonString = jsonAdapter.toJson(signupRequest)

            // JSON 문자열을 RequestBody로 변환
            val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            // 프로필 이미지가 있으면 MultipartBody.Part로 변환
            val profileImagePart = if (profileImageBase64 != null) {
                val imageBytes = android.util.Base64.decode(profileImageBase64, android.util.Base64.DEFAULT)
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", "profile.jpg", requestFile)
            } else {
                null
            }

            // API 호출
            userService.signup(requestBody, profileImagePart)
        } catch (e: Exception) {
            ApiResponse.Error(
                ErrorResponse(
                    status = "NETWORK_ERROR",
                    error = "CONNECTION_FAILED",
                    code = "NETWORK_ERROR",
                    message = "서버에 연결할 수 없습니다: ${e.message}"
                )
            )
        }
    }
}