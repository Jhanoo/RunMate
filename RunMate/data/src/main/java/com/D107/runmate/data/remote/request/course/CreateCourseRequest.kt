package com.D107.runmate.data.remote.request.course

data class CreateCourseRequest(
    val avgElevation: Double,
    val distance: Double,
    val historyId: String,
    val name: String,
    val shared: Boolean,
    val startLocation: String
)