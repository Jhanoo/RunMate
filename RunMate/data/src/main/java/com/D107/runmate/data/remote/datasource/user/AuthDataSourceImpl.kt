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
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.response.user.CheckEmailResponse
import timber.log.Timber

class AuthDataSourceImpl @Inject constructor(
    private val userService: UserService,
    private val moshi: Moshi,
    private val context: Context,
    private val handler: ApiResponseHandler
) : AuthDataSource {
    override suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse> {
        return handler.handle {
            userService.login(loginRequest)
        }
    }

    override suspend fun signup(
        request: SignupRequest,
        profileImageSource: ProfileImageSource?
    ): ApiResponse<SignupResponse> {
        return handler.handle {
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
            userService.signup(userJsonRequestBody, imagePart)
        }
    }

    override suspend fun checkEmail(email: String): ApiResponse<Boolean> {
        return handler.handle {
            userService.checkEmail(email)
        }
    }
}