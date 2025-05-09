package com.D107.runmate.data.remote.response.group

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupMemberData(
    @Json(name = "memberId")
    val memberId: String,
    @Json(name = "nickname")
    val nickname: String,
    @Json(name = "profileImage")
    val profileImage: String
)