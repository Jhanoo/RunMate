package com.D107.runmate.domain.model.manager

data class ScheduleItem(
    val date: String,
    val day: String,
    val scheduleText: String,
    var isCompleted: Boolean,
    val colorIndicator: Int? = null,
    val todoId: String? = null
)