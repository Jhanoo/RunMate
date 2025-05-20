package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class GroupRun(
    val avgPace: Double,
    val courseLiked: Boolean,
    val distance: Double,
    val nickname: String,
    val time: Double,
    val userId: String
): BaseModel
