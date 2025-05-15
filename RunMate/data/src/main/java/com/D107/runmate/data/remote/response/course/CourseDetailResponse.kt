package com.D107.runmate.data.remote.response.course

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.course.CourseDetail
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseDetailResponse(
    val avgElevation: Double,
    val avgEstimatedTime: Int?,
    val distance: Double,
    val gpxFile: String,
    val id: String,
    val liked: Boolean,
    val likes: Int,
    val name: String,
    val shared: Boolean,
    val startLocation: String,
    val userEstimatedTime: Int?
) : BaseResponse {
    companion object : DataMapper<CourseDetailResponse, CourseDetail> {
        override fun CourseDetailResponse.toDomainModel(): CourseDetail {
            return CourseDetail(
                avgElevation = avgElevation,
                avgEstimatedTime = avgEstimatedTime,
                distance = distance,
                gpxFile = gpxFile,
                id = id,
                liked = liked,
                likes = likes,
                name = name,
                shared = shared,
                startLocation = startLocation,
                userEstimatedTime = userEstimatedTime
            )
        }
    }
}