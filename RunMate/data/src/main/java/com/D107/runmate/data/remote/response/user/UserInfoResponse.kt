package com.D107.runmate.data.remote.response.user

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.user.UserInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfoResponse(
    @Json(name = "userId") val userId: String,
    @Json(name = "email") val email: String,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "profileImage") val profileImage: String?,
    @Json(name = "birthday") val birthday: String,
    @Json(name = "gender") val gender: String,
    @Json(name = "weight") val weight: Double,
    @Json(name = "height") val height: Double
): BaseResponse {
    companion object: DataMapper<UserInfoResponse, UserInfo> {
        override fun UserInfoResponse.toDomainModel(): UserInfo {
            return UserInfo(
                userId = userId,
                email = email,
                nickname = nickname,
                profileImage = profileImage,
                birthday = birthday,
                gender = gender,
                weight = weight,
                height = height
            )
        }
    }
}