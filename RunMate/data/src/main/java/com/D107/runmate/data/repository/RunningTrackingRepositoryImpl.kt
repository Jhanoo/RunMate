package com.D107.runmate.data.repository

import android.location.Location
import android.util.Log
import com.D107.runmate.data.mapper.LocationMapper
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.PersonalRunningInfo
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.RunningTrackingState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val TAG = "RunningTrackingRepository"
class RunningTrackingRepositoryImpl @Inject constructor(): RunningTrackingRepository {
    private val _time = MutableStateFlow<Int>(0)
    override val time = _time.asStateFlow()

    private val _runningRecord = MutableStateFlow<RunningRecordState>(RunningRecordState.Initial)
    override val runningRecord = _runningRecord.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocationState>(UserLocationState.Initial)
    override val userLocation = _userLocation.asStateFlow()

    private val _recordSize = MutableStateFlow<Int>(0)
    override val recordSize = _recordSize.asStateFlow()

    private val _trackingStatus = MutableStateFlow(TrackingStatus.INITIAL)
    override val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    override fun setTrackingStatus(status: TrackingStatus) {
        _trackingStatus.value = status
    }

    override fun setInitialUserLocation(location: LocationModel) {
        _userLocation.value = UserLocationState.Exist(listOf(location))
    }

    private var isVibrationEnabled = true
    private var isSoundEnabled = true

    override fun incrementTime() {
        _time.value += 1
    }

    override fun processLocationUpdate(location: LocationModel) {
        when (_userLocation.value) {
            is UserLocationState.Initial -> {
                val personalRunningInfo = PersonalRunningInfo(0f, location.speed, location.altitude, location.speed, location.altitude)
                val tmpList = listOf(personalRunningInfo)
                _runningRecord.value = RunningRecordState.Exist(tmpList)
                _userLocation.value = UserLocationState.Exist(listOf(location))
            }

            is UserLocationState.Exist -> {
                when(val record = _runningRecord.value) {
                    is RunningRecordState.Exist -> {
                        val distanceDiff: Float = LocationMapper.toAndroid(location).distanceTo(LocationMapper.toAndroid((_userLocation.value as UserLocationState.Exist).locations.last()))
                        val distance = distanceDiff + record.runningRecords.last().distance
                        val altitudeSum = record.runningRecords.last().altitude + location.altitude
                        val currentTime = time.value.takeIf { it > 0 } ?: 1
                        val tmpList: List<PersonalRunningInfo> = record.runningRecords +
                                PersonalRunningInfo(
                                    distance / 1000,
                                    distance / currentTime,
                                    location.altitude,
                                    location.speed,
                                    altitudeSum
                                )
                        Log.d(TAG, "processLocationUpdate: ${distance} ${distance/currentTime} ${distanceDiff}")
                        _runningRecord.value = RunningRecordState.Exist(tmpList)
                        _userLocation.value = UserLocationState.Exist((_userLocation.value as UserLocationState.Exist).locations + location)
                    }
                    is RunningRecordState.Initial -> {
                        val personalRunningInfo = PersonalRunningInfo(0f, location.speed, location.altitude, location.speed, location.altitude)
                        val tmpList = listOf(personalRunningInfo)
                        _runningRecord.value = RunningRecordState.Exist(tmpList)
                        _userLocation.value = UserLocationState.Exist(listOf(location))
                    }
                }
            }
        }
    }
}