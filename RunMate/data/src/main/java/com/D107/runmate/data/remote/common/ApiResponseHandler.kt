package com.D107.runmate.data.remote.common

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ApiResponseHandler {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val errorAdapter = moshi.adapter(ErrorResponse::class.java)

    suspend fun <T> handle(call: suspend () -> Response<ApiBaseResponse<T>>): Flow<ApiResponse<T>> {
        return flow {
            val response = call.invoke()
            if (response.isSuccessful) {
                val body = response.body()
                if(body?.data != null) {
                    emit(ApiResponse.Success(body.data))
                } else {
                    emit(
                        ApiResponse.Error(
                            ErrorResponse(
                                status = response.code().toString(),
                                error = "NO_DATA",
                                code = "NO_DATA",
                                message = body?.message ?: "데이터가 없습니다."
                            )
                        )
                    )
                }

            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse: ErrorResponse? = errorBody?.let { errorAdapter.fromJson(it) }

                if (errorResponse != null) {
                    emit(ApiResponse.Error(errorResponse))
                } else {
                    val message = response.message()
                    emit(
                        ApiResponse.Error(
                            ErrorResponse(
                                status = response.code().toString(),
                                error = "UNKNOWN_ERROR",
                                code = "UNKNOWN_CODE",
                                message = message ?: "알 수 없는 오류가 발생했습니다."
                            )
                        )
                    )
                }
            }
        }
    }
}