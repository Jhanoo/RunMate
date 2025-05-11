package com.D107.runmate.data.remote.response.group

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.kakaolocal.PlaceResponse
import com.D107.runmate.domain.model.group.GroupMemberData
import com.D107.runmate.domain.model.group.Place
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupMemberResponse(
    @Json(name = "memberId")
    val memberId: String,
    @Json(name = "nickname")
    val nickname: String,
    @Json(name = "profileImage")
    val profileImage: String
): BaseResponse {
    companion object : DataMapper<GroupMemberResponse, GroupMemberData> {
        override fun GroupMemberResponse.toDomainModel(): GroupMemberData {
            return GroupMemberData(
                memberId = memberId,
                nickname = nickname,
                profileImage = profileImage
            )
        }

    }
}