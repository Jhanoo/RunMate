package com.D107.runmate.data.remote.response.group

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.group.JoinInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class GroupJoinResponse (
    @Json(name = "groupId")
    val groupId:String,
    @Json(name = "groupName")
    val groupName:String,
):BaseResponse{
    companion object : DataMapper<GroupJoinResponse, JoinInfo>{
        override fun GroupJoinResponse.toDomainModel(): JoinInfo {
            return JoinInfo(
                groupId = groupId,
                groupName = groupName
            )
        }


    }
}