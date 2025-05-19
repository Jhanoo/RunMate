package com.D107.runmate.data.remote.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
sealed class ApiResponse<out T> {
    data class Success<out T>(
        @Json(name = "message") val message: String,
        @Json(name = "data") val data: T?
    ): ApiResponse<T>()

    data class Error(
        @Json(name = "error") val error: ErrorResponse
    ): ApiResponse<Nothing>()
}