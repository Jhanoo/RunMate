package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.course.CourseDataSource
import com.D107.runmate.data.remote.request.course.CreateCourseRequest
import com.D107.runmate.data.remote.response.course.CourseDetailResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.course.CourseItemResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.course.CourseLikeResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseDetail
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.course.CourseLike
import com.D107.runmate.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseDataSource: CourseDataSource
) : CourseRepository {
    override suspend fun getAllCourseList(): Flow<ResponseStatus<List<CourseInfo>>> {
        return flow {
            try{
                when (val response = courseDataSource.getAllCourseList()) {
                    is ApiResponse.Error -> emit(
                        ResponseStatus.Error(
                            NetworkError(
                                error = response.error.error ?: "UNKNOWN_ERROR",
                                code = response.error.code ?: "UNKNOWN_CODE",
                                status = response.error.status ?: "ERROR",
                                message = response.error.message ?: "코스 전체 조회에 실패했습니다"
                            )
                        )
                    )

                    is ApiResponse.Success -> {
                        val courseInfoList = response.data.map { it.toDomainModel() }
                        emit(ResponseStatus.Success(courseInfoList))
                    }
                }
            }catch (e: Exception) {
                Timber.e("${e.message}")
                emit(
                    ResponseStatus.Error(
                        NetworkError(
                            message = e.message ?: "",
                        )
                    )
                )
            }
        }
    }

    override suspend fun searchCourse(keyword: String): Flow<ResponseStatus<List<CourseInfo>>> {
        return flow {
            try {
                when (val response = courseDataSource.searchCourseList(keyword = keyword)) {
                    is ApiResponse.Error -> emit(
                        ResponseStatus.Error(
                            NetworkError(
                                error = response.error.error ?: "UNKNOWN_ERROR",
                                code = response.error.code ?: "UNKNOWN_CODE",
                                status = response.error.status ?: "ERROR",
                                message = response.error.message ?: "코스 키워드 조회에 실패했습니다"
                            )
                        )
                    )

                    is ApiResponse.Success -> {
                        val courseInfoList = response.data.map { it.toDomainModel() }
                        emit(ResponseStatus.Success(courseInfoList))
                    }
                }
            } catch (e: Exception) {
                Timber.e("${e.message}")
                emit(
                    ResponseStatus.Error(
                        NetworkError(
                            message = e.message ?: "",
                        )
                    )
                )
            }

        }
    }

    override suspend fun getCourseDetail(courseId: String): Flow<ResponseStatus<CourseDetail>> {
        return flow {
            when (val response = courseDataSource.getCourseDetail(courseId = courseId)) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "코스 상세 조회에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> emit(ResponseStatus.Success(response.data.toDomainModel()))
            }
        }
    }

    override suspend fun updateCourseLike(courseId: String): Flow<ResponseStatus<CourseLike>> {
        return flow {
            when (val response = courseDataSource.updateCourseLike(courseId = courseId)) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "코스 좋아요 업데이트에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> emit(ResponseStatus.Success(response.data.toDomainModel()))
            }
        }
    }

    override suspend fun deleteCourse(courseId: String): Flow<ResponseStatus<Any?>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMyCourse(): Flow<ResponseStatus<List<CourseInfo>>> {
        return flow {
            when (val response = courseDataSource.getMyCourseList()) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "내가 만든 코스 조회에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> {
                    val courseInfoList = response.data.map { it.toDomainModel() }
                    emit(ResponseStatus.Success(courseInfoList))
                }
            }
        }
    }

    override suspend fun getRecentCourse(): Flow<ResponseStatus<List<CourseInfo>>> {
        return flow {
            when (val response = courseDataSource.getRecentCourseList()) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "최근 달린 코스 조회에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> {
                    val courseInfoList = response.data.map { it.toDomainModel() }
                    emit(ResponseStatus.Success(courseInfoList))
                }
            }
        }
    }

    override suspend fun createCourse(
        avgElevation: Double,
        distance: Float,
        historyId: String,
        name: String,
        shared: Boolean,
        startLocation: String
    ): Flow<ResponseStatus<String>> {
        return flow {
            val request =
                CreateCourseRequest(avgElevation, distance, historyId, name, shared, startLocation)
            when (val response = courseDataSource.createCourse(request)) {
                is ApiResponse.Error -> emit(
                    ResponseStatus.Error(
                        NetworkError(
                            error = response.error.error ?: "UNKNOWN_ERROR",
                            code = response.error.code ?: "UNKNOWN_CODE",
                            status = response.error.status ?: "ERROR",
                            message = response.error.message ?: "코스 생성에 실패했습니다"
                        )
                    )
                )

                is ApiResponse.Success -> emit(ResponseStatus.Success(response.data.courseId))
            }
        }
    }

}