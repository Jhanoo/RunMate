package com.D107.runmate.data.remote.common

import com.squareup.moshi.Json

data class ApiBaseResponse<T>(
    @Json(name = "message") val message: String?,
    @Json(name = "data") val data: T?
)