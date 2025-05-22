package com.D107.runmate.data.remote.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
class ApiResponseHandler(private val moshi: Moshi) {

    suspend fun <T : Any> handle(
        apiCall: suspend () -> Response<ServerResponse<T>>
    ): ApiResponse<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                parseSuccessResponse(response)
            } else {
                parseErrorResponse(response)
            }
        } catch (e: IOException) {
            createNetworkError(e)
        } catch (e: JsonDataException) {
            createUnknownError(e)
        }
        catch (e: Exception) {
            createUnknownError(e)
        }
    }

    private fun <T : Any> parseSuccessResponse(response: Response<ServerResponse<T>>): ApiResponse<T> {
        val serverResponse = response.body()
        if (serverResponse != null) {
            if(serverResponse.data == null){
                return ApiResponse.Success(serverResponse.message, null)
            } else {
                return ApiResponse.Success(serverResponse.message, serverResponse.data)
            }
        } else {
            return ApiResponse.Error(createEmptyBodyError(response))
        }
    }

    private fun <T : Any> parseErrorResponse(response: Response<ServerResponse<T>>): ApiResponse<T> {
        return try {
            val errorBody = response.errorBody()?.source()
            val errorAdapter = moshi.adapter(ErrorResponse::class.java)
            val errorResponse = errorBody?.let { errorAdapter.fromJson(it) }
            ApiResponse.Error(errorResponse ?: createDefaultError(response))
        } catch (e: Exception) {
            ApiResponse.Error(createParseError(response, e))
        }
    }

    // 에러 생성 유틸리티 함수들
    private fun createNetworkError(e: IOException): ApiResponse.Error {
        return ApiResponse.Error(
            ErrorResponse(
                status = "NETWORK_ERROR",
                error = "NETWORK_EXCEPTION",
                code = "-1",
                message = e.message ?: "Network error occurred"
            )
        )
    }

    private fun createUnknownError(e: Exception): ApiResponse.Error {
        Timber.d("createUnknownError: ${e.message}")
        return ApiResponse.Error(
            ErrorResponse(
                status = "UNKNOWN_ERROR",
                error = "UNKNOWN_EXCEPTION",
                code = "-1",
                message = e.message ?: "Unknown error occurred"
            )
        )
    }

    private fun createEmptyBodyError(response: Response<*>): ErrorResponse {
        return ErrorResponse(
            status = "EMPTY_BODY",
            error = "EMPTY_RESPONSE",
            code = response.code().toString(),
            message = "Response body is empty"
        )
    }

    private fun createDefaultError(response: Response<*>): ErrorResponse {
        return ErrorResponse(
            status = "HTTP_ERROR",
            error = "UNPARSABLE_ERROR",
            code = response.code().toString(),
            message = response.message()
        )
    }

    private fun createParseError(response: Response<*>, e: Exception): ErrorResponse {
        return ErrorResponse(
            status = "PARSING_ERROR",
            error = "ERROR_PARSE_FAILURE",
            code = response.code().toString(),
            message = "Failed to parse error response: ${e.message}"
        )
    }
}

@JsonClass(generateAdapter = true)
data class ServerResponse<T>(
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: T?
)