package com.D107.runmate.watch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GpxEntity): Long

    @Query("UPDATE gpx_files SET status = :status, last_attempt = :time WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, time: Long)

    @Query("SELECT * FROM gpx_files WHERE status IN ('WAITING', 'FAILED')")
    fun getPendingFiles(): Flow<List<GpxEntity>>

    @Query("SELECT * FROM gpx_files ORDER BY created_at DESC")
    fun getAllFiles(): Flow<List<GpxEntity>>

    @Query("SELECT * FROM gpx_files WHERE id = :id")
    suspend fun getGpxFileById(id: Long): GpxEntity?

    @Query("UPDATE gpx_files SET status = 'UPLOADING' WHERE id = :id")
    suspend fun markAsUploading(id: Long)

    @Query("DELETE FROM gpx_files WHERE id = :id")
    suspend fun deleteFile(id: Long)
}