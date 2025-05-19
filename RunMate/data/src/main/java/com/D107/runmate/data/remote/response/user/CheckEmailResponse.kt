package com.D107.runmate.data.remote.response.user

import com.D107.runmate.data.remote.common.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheckEmailResponse(
    @Json(name = "data") val isDuplicated: Boolean,
    @Json(name = "message") val message: String
) : BaseResponse