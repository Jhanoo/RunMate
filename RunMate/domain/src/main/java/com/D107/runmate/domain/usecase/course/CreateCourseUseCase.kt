package com.D107.runmate.domain.usecase.course

import com.D107.runmate.domain.repository.course.CourseRepository
import javax.inject.Inject

class CreateCourseUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(
        avgElevation: Double,
        distance: Float,
        historyId: String,
        name: String,
        shared: Boolean,
        startLocation: String
    ) = repository.createCourse(avgElevation, distance, historyId, name, shared, startLocation)
}