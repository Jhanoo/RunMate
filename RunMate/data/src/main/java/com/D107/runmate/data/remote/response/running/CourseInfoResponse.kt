package com.D107.runmate.data.remote.response.running

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.data.remote.response.running.Creator.Companion.toDomainModel
import com.D107.runmate.domain.model.running.CourseInfo

data class CourseInfoResponse(
    val avgElevation: Double,
    val courseId: String,
    val courseName: String,
    val creator: Creator,
    val distance: Double,
    val likeCount: Int,
    val shared: Boolean,
    val startLocation: String
): BaseResponse {
    companion object: DataMapper<CourseInfoResponse, CourseInfo> {
        override fun CourseInfoResponse.toDomainModel(): CourseInfo {
            return CourseInfo(
                avgElevation = avgElevation,
                courseId = courseId,
                courseName = courseName,
                creator = creator.toDomainModel(),
                distance = distance,
                likeCount = likeCount,
                shared = shared,
                startLocation = startLocation
            )
        }
    }
}