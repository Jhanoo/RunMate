package com.D107.runmate.domain.model.course

import com.D107.runmate.domain.model.base.BaseModel

data class CourseInfo(
    val avgElevation: Double,
    val courseId: String,
    val courseName: String,
    val creator: Creator,
    val distance: Double,
    val likeCount: Int,
    val liked: Boolean,
    val shared: Boolean,
    val startLocation: String
): BaseModel