package com.D107.runmate.domain.model.running

import com.D107.runmate.domain.model.base.BaseModel

data class CourseInfo(
    val avgElevation: Double,
    val courseId: String,
    val courseName: String,
    val creator: Creator,
    val distance: Double,
    val likeCount: Int,
    val shared: Boolean,
    val liked: Boolean,
    val startLocation: String
) : BaseModel

