package com.D107.runmate.data.remote.datasource.user

import android.net.Uri
import com.D107.runmate.data.remote.api.UserService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.request.user.LoginRequest
import com.D107.runmate.data.remote.request.user.SignupRequest
import com.D107.runmate.data.remote.response.user.LoginResponse
import com.D107.runmate.data.remote.response.user.SignupResponse
import com.D107.runmate.domain.model.user.ProfileImageSource
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import android.content.Context
import com.D107.runmate.data.remote.response.user.CheckEmailResponse
import timber.log.Timber

class AuthDataSourceImpl @Inject constructor(
    private val userService: UserService,
    private val moshi: Moshi,
    private val context: Context
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

    override suspend fun signup(
        request: SignupRequest,
        profileImageSource: ProfileImageSource?
    ): ApiResponse<SignupResponse> {
        try {
            // 'data' part에 JSON으로 사용자 정보 추가
            val jsonAdapter = moshi.adapter(SignupRequest::class.java)
            val userJsonRequestBody = jsonAdapter.toJson(request)
                .toRequestBody("application/json".toMediaTypeOrNull())

            // 프로필 이미지가 있으면 처리
            val imagePart = profileImageSource?.let { source ->
                // Data 레이어에서 Uri 변환
                val uri = Uri.parse(source.imageId)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profileImage", "profile.jpg", requestFile)
                }
            }

            // API 호출 (imagePart가 null일 경우 이미지 없이 요청)
            return userService.signup(userJsonRequestBody, imagePart)
        } catch (e: Exception) {
            return ApiResponse.Error(
                ErrorResponse(
                    message = e.message ?: "회원가입에 실패했습니다.",
                    status = "ERROR"
                )
            )
        }
    }

    override suspend fun checkEmail(email: String): ApiResponse<CheckEmailResponse> {
        return try {
            val response = userService.checkEmail(email)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResponse.Success(
                        CheckEmailResponse(
                            isDuplicated = body.data,
                            message = body.message
                        )
                    )
                } else {
                    ApiResponse.Error(
                        ErrorResponse(
                            status = "ERROR",
                            error = "NULL_RESPONSE",
                            code = "NULL_RESPONSE",
                            message = "응답이 비어있습니다."
                        )
                    )
                }
            } else {
                ApiResponse.Error(
                    ErrorResponse(
                        status = "ERROR",
                        error = "HTTP_ERROR",
                        code = response.code().toString(),
                        message = "HTTP 에러: ${response.code()}"
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e("Email check exception: ${e.message}")
            e.printStackTrace()
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