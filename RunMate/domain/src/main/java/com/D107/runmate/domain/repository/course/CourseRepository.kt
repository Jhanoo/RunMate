package com.D107.runmate.domain.repository.course

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseDetail
import com.D107.runmate.domain.model.course.CourseInfo
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    suspend fun getAllCourseList(): Flow<ResponseStatus<List<CourseInfo>>>
    suspend fun searchCourse(keyword: String): Flow<ResponseStatus<List<CourseInfo>>>
    suspend fun getCourseDetail(courseId: String): Flow<ResponseStatus<CourseDetail>>
    suspend fun updateCourseLike(courseId: String): Flow<ResponseStatus<CourseInfo>>
    suspend fun deleteCourse(courseId: String): Flow<ResponseStatus<Any?>>
    suspend fun getMyCourse(): Flow<ResponseStatus<List<CourseInfo>>>
    suspend fun getRecentCourse(): Flow<ResponseStatus<List<CourseInfo>>>
}