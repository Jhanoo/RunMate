package com.D107.runmate.data.remote.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


sealed class ApiResponse<out T> {
    @JsonClass(generateAdapter = true)
    data class Success<T>(
        @Json(name = "data")val data: T
    ): ApiResponse<T>()
    @JsonClass(generateAdapter = true)
    data class Error(@Json(name = "error")val error: ErrorResponse): ApiResponse<Nothing>()
}