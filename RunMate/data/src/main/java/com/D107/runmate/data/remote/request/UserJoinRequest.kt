package com.D107.runmate.data.remote.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserJoinRequest(
    @Json(name = "birthYear") val birthYear: Int,
    @Json(name = "fcmToken") val fcmToken: String,
    @Json(name = "paymentPassword") val paymentPassword: Int,
    @Json(name = "userJob") val userJob: String
)