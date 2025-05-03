package com.D107.runmate.data.remote.response.user

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.user.UserInfo

data class UserInfoResponse(
    val birthYear: Int,
    val nickname: String,
    val userId: Int,
    val userJob: String
): BaseResponse {
    companion object: DataMapper<UserInfoResponse, UserInfo> {
        override fun UserInfoResponse.toDomainModel(): UserInfo {
            return UserInfo(
                birthYear = birthYear,
                nickname = nickname,
                userId = userId,
                userJob = userJob
            )
        }
    }
}