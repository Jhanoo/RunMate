package com.D107.runmate.data.remote.response.course

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.course.CourseItemResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.course.Creator.Companion.toDomainModel
import com.D107.runmate.domain.model.course.CourseInfo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseItemResponse(
    val avgElevation: Double,
    val courseId: String,
    val courseName: String,
    val creator: Creator,
    val distance: Double,
    val likeCount: Int,
    val liked: Boolean,
    val shared: Boolean,
    val startLocation: String
): BaseResponse {
    companion object: DataMapper<CourseItemResponse, CourseInfo> {
        override fun CourseItemResponse.toDomainModel(): CourseInfo {
            return CourseInfo(
                avgElevation = avgElevation,
                courseId = courseId,
                courseName = courseName,
                creator = creator.toDomainModel(),
                distance = distance,
                likeCount = likeCount,
                shared = shared,
                liked = liked,
                startLocation = startLocation
            )
        }
    }
}

