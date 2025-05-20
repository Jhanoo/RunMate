package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class MyRun(
    val addedToCourse: Boolean,
    val avgBpm: Double,
    val avgPace: Double,
    val avgCadence: Double,
    val avgElevation: Double,
    val calories: Double,
    val courseLiked: Boolean,
    val courseLikes: Int,
    val distance: Double,
    val time: Long
): BaseModel
