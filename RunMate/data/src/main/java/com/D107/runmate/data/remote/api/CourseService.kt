package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.course.CreateCourseRequest
import com.D107.runmate.data.remote.response.course.CourseDetailResponse
import com.D107.runmate.data.remote.response.course.CourseIdResponse
import com.D107.runmate.data.remote.response.course.CourseItemResponse
import com.D107.runmate.data.remote.response.course.CourseLikeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseService {
    @POST("courses/create")
    suspend fun createCourse(@Body createCourseRequest: CreateCourseRequest): ApiResponse<CourseIdResponse>

    @PATCH("courses/{courseId}/like")
    suspend fun updateCourseLike(@Path("courseId") courseId: String): ApiResponse<CourseLikeResponse>

    @GET("courses/{courseId}")
    suspend fun getCourseDetail(@Path("courseId") courseId: String): ApiResponse<CourseDetailResponse>

    @DELETE("courses/{courseId}")
    suspend fun deleteCourse(@Path("courseId") courseId: String): ApiResponse<Any?>

    @GET("courses/search")
    suspend fun searchCourse(@Query("keyword") keyword: String): ApiResponse<List<CourseItemResponse>>

    @GET("courses/all")
    suspend fun getAllCourseList(): ApiResponse<List<CourseItemResponse>>

    @GET("courses/recent")
    suspend fun getRecentCourseList(): ApiResponse<List<CourseItemResponse>>

    @GET("courses/my")
    suspend fun getMyCourseList(): ApiResponse<List<CourseItemResponse>>


}