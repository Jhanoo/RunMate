package com.D107.runmate.data.remote.response.user

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.user.UserInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignupResponse(
    @Json(name = "userId") val userId: String,
    @Json(name = "email") val email: String,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "profileImage") val profileImage: String?,
    @Json(name = "birthday") val birthday: String,
    @Json(name = "gender") val gender: String
) : BaseResponse {
    companion object : DataMapper<SignupResponse, UserInfo> {
        override fun SignupResponse.toDomainModel(): UserInfo {
            return UserInfo(
                userId = userId,
                email = email,
                nickname = nickname,
                profileImage = profileImage,
                birthday = birthday,
                gender = gender
            )
        }
    }
}