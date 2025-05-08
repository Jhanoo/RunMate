package com.D107.runmate.data.remote.response.running

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.base.BaseModel

data class Creator(
    val nickname: String,
    val profileImage: String
): BaseResponse {
    companion object : DataMapper<Creator, com.D107.runmate.domain.model.running.Creator> {
        override fun Creator.toDomainModel(): com.D107.runmate.domain.model.running.Creator {
            return com.D107.runmate.domain.model.running.Creator(
                nickname = nickname,
                profileImage = profileImage
            )
        }

    }
}