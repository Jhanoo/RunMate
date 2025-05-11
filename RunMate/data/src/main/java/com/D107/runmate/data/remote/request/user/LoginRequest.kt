package com.D107.runmate.data.remote.request.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)