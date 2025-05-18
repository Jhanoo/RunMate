package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class MyRun(
    val addedToCourse: Boolean,
    val avgBpm: Int,
    val avgPace: Int,
    val calories: Int,
    val courseLiked: Boolean,
    val courseLikes: Int,
    val distance: Int,
    val time: Int
): BaseModel
