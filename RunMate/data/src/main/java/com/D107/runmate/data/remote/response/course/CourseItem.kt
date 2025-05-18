package com.D107.runmate.data.remote.response.course

import com.D107.runmate.domain.model.base.BaseModel

data class CourseItem(
    val avgElevation: Double,
    val courseId: String,
    val courseName: String?,
    val creator: Creator,
    val distance: Double,
    val likeCount: Int,
    val liked: Boolean,
    val shared: Boolean,
    val startLocation: String
): BaseModel