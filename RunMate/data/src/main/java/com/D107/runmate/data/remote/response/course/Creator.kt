package com.D107.runmate.data.remote.response.course

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse

data class Creator(
    val nickname: String,
    val profileImage: String?
):BaseResponse {
    companion object: DataMapper<Creator, com.D107.runmate.domain.model.course.Creator> {
        override fun Creator.toDomainModel(): com.D107.runmate.domain.model.course.Creator {
            return com.D107.runmate.domain.model.course.Creator(nickname, profileImage)
        }

    }
}