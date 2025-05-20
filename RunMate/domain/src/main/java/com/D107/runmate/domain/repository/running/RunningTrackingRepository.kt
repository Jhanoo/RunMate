package com.D107.runmate.domain.repository.running

import com.D107.runmate.domain.model.running.CadenceRecordState
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RunningTrackingRepository {
    val runningRecord: StateFlow<RunningRecordState>
    val userLocation: StateFlow<UserLocationState>
    val cadenceRecord: StateFlow<CadenceRecordState>
    val time: StateFlow<Int>
    val recordSize: StateFlow<Int>
    val trackingStatus: StateFlow<TrackingStatus>
    val historyId: StateFlow<String?>
    val courseId: StateFlow<String?>
    val goalPace: StateFlow<Int?>
    val isSound: StateFlow<Boolean>
    val isVibration: StateFlow<Boolean>

    fun incrementTime()
    fun processLocationUpdate(location: LocationModel, cadence: Int)
    fun setTrackingStatus(status: TrackingStatus)
    fun setInitialUserLocation(location: LocationModel)
    fun finishTracking(): Flow<Boolean>

    fun deleteFile(): Flow<Boolean>
    fun setHistoryId(historyId: String?)
    fun setCourseId(courseId: String?)
    fun setGoalPace(goalPace: Int)
    fun toggleIsSound()
    fun toggleIsVibration()

}