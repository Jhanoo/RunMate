package com.D107.runmate.watch.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gpx_files")
data class GpxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "last_attempt")
    val lastAttempt: Long?,

    @ColumnInfo(name = "total_distance")
    val totalDistance: Double,

    @ColumnInfo(name = "total_time")
    val totalTime: Long,

    @ColumnInfo(name = "avg_heart_rate")
    val avgHeartRate: Int,

    @ColumnInfo(name = "max_heart_rate")
    val maxHeartRate: Int
)