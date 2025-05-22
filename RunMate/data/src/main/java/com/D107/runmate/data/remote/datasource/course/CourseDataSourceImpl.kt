package com.D107.runmate.data.remote.datasource.course

import com.D107.runmate.data.remote.api.CourseService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.request.course.CreateCourseRequest
import com.D107.runmate.data.remote.response.course.CourseDetailResponse
import com.D107.runmate.data.remote.response.course.CourseIdResponse
import com.D107.runmate.data.remote.response.course.CourseItemResponse
import com.D107.runmate.data.remote.response.course.CourseLikeResponse
import javax.inject.Inject

class CourseDataSourceImpl @Inject constructor(
    private val courseService: CourseService,
    private val handler: ApiResponseHandler
): CourseDataSource {
    override suspend fun createCourse(createCourseRequest: CreateCourseRequest): ApiResponse<CourseIdResponse> {
        return handler.handle {
            courseService.createCourse(createCourseRequest)
        }
    }

    override suspend fun getAllCourseList(): ApiResponse<List<CourseItemResponse>> {
        return handler.handle {
            courseService.getAllCourseList()
        }
    }

    override suspend fun searchCourseList(keyword: String): ApiResponse<List<CourseItemResponse>> {
        return handler.handle {
            courseService.searchCourse(keyword)
        }
    }

    override suspend fun getRecentCourseList(): ApiResponse<List<CourseItemResponse>> {
        return handler.handle {
            courseService.getRecentCourseList()
        }
    }

    override suspend fun getMyCourseList(): ApiResponse<List<CourseItemResponse>> {
        return handler.handle {
            courseService.getMyCourseList()
        }
    }

    override suspend fun getCourseDetail(courseId: String): ApiResponse<CourseDetailResponse> {
        return handler.handle {
            courseService.getCourseDetail(courseId)
        }
    }

    override suspend fun updateCourseLike(courseId: String): ApiResponse<CourseLikeResponse> {
        return handler.handle {
            courseService.updateCourseLike(courseId)
        }
    }

    override suspend fun deleteCourse(courseId: String): ApiResponse<Any?> {
        TODO()
    }

}