package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.history.GroupRun
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroupRunItem(
    val avgPace: Int,
    val courseLiked: Boolean,
    val distance: Int,
    val nickname: String,
    val time: Int,
    val userId: String
): BaseResponse {
    companion object: DataMapper<GroupRunItem, GroupRun> {
        override fun GroupRunItem.toDomainModel(): GroupRun {
            return GroupRun(avgPace, courseLiked, distance, nickname, time, userId)
        }
    }
}
