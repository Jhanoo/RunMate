package com.D107.runmate.data.remote.response.course

import com.D107.runmate.data.remote.common.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseIdResponse(
    @Json(name = "courseId")val courseId: String
):BaseResponse
