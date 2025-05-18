package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class History(
    val courseName: String?,
    val duration: Int,
    val groupName: String?,
    val historyId: String,
    val location: String,
    val members: List<String>,
    val myDistance: Double,
    val startTime: String
): BaseModel
