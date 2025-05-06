package com.D107.runmate.watch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_points")
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val time: Long,
    val heartRate: Int,
    val cadence: Int,
    val pace: String
)