package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ServerResponse
import com.D107.runmate.data.remote.request.course.CreateCourseRequest
import com.D107.runmate.data.remote.response.course.CourseDetailResponse
import com.D107.runmate.data.remote.response.course.CourseIdResponse
import com.D107.runmate.data.remote.response.course.CourseItemResponse
import com.D107.runmate.data.remote.response.course.CourseLikeResponse
import retrofit2.Response
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
    suspend fun createCourse(@Body createCourseRequest: CreateCourseRequest): Response<ServerResponse<CourseIdResponse>>

    @PATCH("courses/{courseId}/like")
    suspend fun updateCourseLike(@Path("courseId") courseId: String): Response<ServerResponse<CourseLikeResponse>>

    @GET("courses/{courseId}")
    suspend fun getCourseDetail(@Path("courseId") courseId: String): Response<ServerResponse<CourseDetailResponse>>

    @DELETE("courses/{courseId}")
    suspend fun deleteCourse(@Path("courseId") courseId: String): Response<ServerResponse<Any?>>

    @GET("courses/search")
    suspend fun searchCourse(@Query("keyword") keyword: String): Response<ServerResponse<List<CourseItemResponse>>>

    @GET("courses/all")
    suspend fun getAllCourseList(): Response<ServerResponse<List<CourseItemResponse>>>

    @GET("courses/recent")
    suspend fun getRecentCourseList(): Response<ServerResponse<List<CourseItemResponse>>>

    @GET("courses/my")
    suspend fun getMyCourseList(): Response<ServerResponse<List<CourseItemResponse>>>


}