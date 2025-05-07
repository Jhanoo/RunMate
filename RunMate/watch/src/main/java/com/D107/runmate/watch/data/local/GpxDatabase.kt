package com.D107.runmate.watch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GpxEntity::class, TrackPointEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GpxDatabase : RoomDatabase() {
    abstract fun gpxDao(): GpxDao

    companion object {
        @Volatile
        private var INSTANCE: GpxDatabase? = null

        fun getDatabase(context: Context): GpxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GpxDatabase::class.java,
                    "gpx_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}