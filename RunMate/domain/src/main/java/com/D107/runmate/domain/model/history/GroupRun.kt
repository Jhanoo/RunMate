package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class GroupRun(
    val avgPace: Int,
    val courseLiked: Boolean,
    val distance: Int,
    val nickname: String,
    val time: Int,
    val userId: String
): BaseModel
