package com.D107.runmate.data.remote.response.history

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.history.MyRun
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MyRunItem(
    val addedToCourse: Boolean,
    val avgBpm: Double,
    val avgPace: Double,
    val calories: Double,
    val courseLiked: Boolean,
    val courseLikes: Int,
    val distance: Double,
    val time: Long
): BaseResponse {
    companion object: DataMapper<MyRunItem, MyRun> {
        override fun MyRunItem.toDomainModel(): MyRun {
            return MyRun(addedToCourse, avgBpm, avgPace, calories, courseLiked, courseLikes, distance, time)
        }
    }
}