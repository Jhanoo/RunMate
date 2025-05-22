package com.D107.runmate.domain.model.base

sealed class ResponseStatus<out T> {
    data class Success<T>(val data: T): ResponseStatus<T>()
    data class Error(val error: NetworkError): ResponseStatus<Nothing>()
}