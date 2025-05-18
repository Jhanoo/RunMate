package com.D107.runmate.domain.usecase.course

import com.D107.runmate.domain.repository.course.CourseRepository
import javax.inject.Inject

class SearchCourseUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(keyword: String) = courseRepository.searchCourse(keyword)
}