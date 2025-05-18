package com.D107.runmate.data.remote.response.course

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.course.CourseLike
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseLikeResponse(
    @Json(name = "liked")val liked: Boolean,
    @Json(name = "totalLikes")val totalLikes: Int
): BaseResponse {
    companion object: DataMapper<CourseLikeResponse, CourseLike> {
        override fun CourseLikeResponse.toDomainModel(): CourseLike {
            return CourseLike(liked, totalLikes)
        }
    }
}