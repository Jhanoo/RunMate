package com.D107.runmate.data.remote.datasource.course

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.course.CreateCourseRequest
import com.D107.runmate.data.remote.response.course.CourseDetailResponse
import com.D107.runmate.data.remote.response.course.CourseIdResponse
import com.D107.runmate.data.remote.response.course.CourseItemResponse
import com.D107.runmate.data.remote.response.course.CourseLikeResponse

interface CourseDataSource {
    suspend fun createCourse(createCourseRequest: CreateCourseRequest): ApiResponse<CourseIdResponse>
    suspend fun updateCourseLike(courseId: String): ApiResponse<CourseLikeResponse>
    suspend fun getCourseDetail(courseId: String): ApiResponse<CourseDetailResponse>
    suspend fun deleteCourse(courseId: String): ApiResponse<Any?>
    suspend fun searchCourseList(keyword: String): ApiResponse<List<CourseItemResponse>>
    suspend fun getRecentCourseList(): ApiResponse<List<CourseItemResponse>>
    suspend fun getMyCourseList(): ApiResponse<List<CourseItemResponse>>
    suspend fun getAllCourseList(): ApiResponse<List<CourseItemResponse>>
}