package com.D107.runmate.domain.usecase.course

import com.D107.runmate.domain.repository.course.CourseRepository
import javax.inject.Inject

class GetRecentCourseUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke() = courseRepository.getRecentCourse()
}