package com.D107.runmate.data.remote.common

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T): ApiResponse<T>()
    data class Error(val error: ErrorResponse): ApiResponse<Nothing>()
}