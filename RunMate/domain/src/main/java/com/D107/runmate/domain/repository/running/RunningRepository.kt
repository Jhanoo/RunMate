package com.D107.runmate.domain.repository.running

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.running.EndRunning
import kotlinx.coroutines.flow.Flow

interface RunningRepository {
    suspend fun endRunning(
        avgBpm: Double,
        avgCadence: Double,
        avgElevation: Double,
        avgPace: Double,
        calories: Double,
        courseId: String?,
        distance: Double,
        endTime: String,
        startLocation: String,
        startTime: String,
        groupId:String?
    ): Flow<ResponseStatus<EndRunning>>
}