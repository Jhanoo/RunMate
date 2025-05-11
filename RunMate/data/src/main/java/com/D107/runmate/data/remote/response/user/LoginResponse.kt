package com.D107.runmate.data.remote.response.user

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.user.LoginData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "accessToken") val accessToken: String
) : BaseResponse {
    companion object : DataMapper<LoginResponse, LoginData> {
        override fun LoginResponse.toDomainModel(): LoginData {
            return LoginData(
                accessToken = accessToken
            )
        }
    }
}