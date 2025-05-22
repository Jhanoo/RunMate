package com.D107.runmate.data.remote.request.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody

@JsonClass(generateAdapter = true)
data class SignupRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "birthday") val birthday: String,
    @Json(name = "gender") val gender: String,
    @Json(name = "profileImage") val profileImage: String? = null,
    @Json(name = "weight") val weight: Double,
    @Json(name = "height") val height: Double,
)