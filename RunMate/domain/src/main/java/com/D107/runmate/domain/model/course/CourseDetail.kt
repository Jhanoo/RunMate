package com.D107.runmate.domain.model.course

import com.D107.runmate.domain.model.base.BaseModel

data class CourseDetail(
    val avgElevation: Double,
    val avgEstimatedTime: Int?,
    val distance: Double,
    val gpxFile: String,
    val id: String,
    val liked: Boolean,
    val likes: Int,
    val name: String,
    val shared: Boolean,
    val startLocation: String,
    val userEstimatedTime: Int?
): BaseModel
