package com.D107.runmate.domain.usecase.running

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.repository.running.RunningRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EndRunningUseCase @Inject constructor(
    private val runningRepository: RunningRepository
) {
    suspend operator fun invoke(
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
        groupId:String?=null
    ) = runningRepository.endRunning(
        avgBpm,
        avgCadence,
        avgElevation,
        avgPace,
        calories,
        courseId,
        distance,
        endTime,
        startLocation,
        startTime,
        groupId
    )
}