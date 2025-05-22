package com.D107.runmate.data.remote.common

import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
//
//class ApiResponseAdapterFactory : JsonAdapter.Factory {
//    override fun create(
//        type: Type,
//        annotations: Set<Annotation>,
//        moshi: Moshi
//    ): JsonAdapter<*>? {
//        if (annotations.isNotEmpty() || Types.getRawType(type) != ApiResponse::class.java) {
//            return null // 이 팩토리가 처리할 타입이 아님
//        }
//
//        // ApiResponse<T> 에서 T 타입을 가져옴
//        val dataType = (type as ParameterizedType).actualTypeArguments[0]
//        val dataAdapter = moshi.adapter<Any>(dataType) // T 타입에 대한 어댑터
//        val errorAdapter = moshi.adapter(ErrorResponse::class.java) // ErrorResponse에 대한 어댑터
//
//        return ApiResponseAdapter(dataAdapter, errorAdapter)
//    }
//
//    private class ApiResponseAdapter<T>(
//        private val dataAdapter: JsonAdapter<T>,
//        private val errorAdapter: JsonAdapter<ErrorResponse>
//    ) : JsonAdapter<ApiResponse<T>>() {
//
//        override fun fromJson(reader: JsonReader): ApiResponse<T>? {
//
//            val jsonValue = reader.readJsonValue() // 전체 JSON 객체를 읽음
//
//            if (jsonValue !is Map<*, *>) {
//                throw JsonDataException("Expected a JSON object for ApiResponse at ${reader.path}")
//            }
//
//            if (jsonValue.containsKey("data")) {
//                val dataJsonElement = jsonValue["data"]
//                val data = dataAdapter.fromJsonValue(dataJsonElement)
//                @Suppress("UNCHECKED_CAST")
//                return ApiResponse.Success(data as T)
//            }
//
//            // 'error' 필드가 있는지 확인
//            if (jsonValue.containsKey("error")) {
//                val error = errorAdapter.fromJsonValue(jsonValue["error"])
//                if (error != null) {
//                    return ApiResponse.Error(error)
//                }
//            }
//
//            throw JsonDataException(
//                "ApiResponse must contain 'data' for Success or 'error' for Error. Received: $jsonValue at ${reader.path}"
//            )
//        }
//
//        override fun toJson(writer: JsonWriter, value: ApiResponse<T>?) {
//            if (value == null) {
//                writer.nullValue()
//                return
//            }
//            writer.beginObject()
//            when (value) {
//                is ApiResponse.Success -> {
//                    writer.name("data")
//                    dataAdapter.toJson(writer, value.data)
//                }
//                is ApiResponse.Error -> {
//                    writer.name("error")
//                    errorAdapter.toJson(writer, value.error)
//                }
//            }
//            writer.endObject()
//        }
//    }
//}