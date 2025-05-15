package com.D107.runmate.data.remote.response.course

import com.D107.runmate.data.remote.common.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseLikeResponse(
    @Json(name = "liked")val liked: Boolean,
    @Json(name = "totalLikes")val totalLikes: Int
): BaseResponse