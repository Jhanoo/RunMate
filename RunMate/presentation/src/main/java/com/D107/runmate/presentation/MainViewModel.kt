package com.D107.runmate.presentation

import androidx.lifecycle.ViewModel
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val TAG = "MainViewModel"
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RunningTrackingRepository
): ViewModel() {
    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled = _isVibrationEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _courseId = MutableStateFlow<String?>(null) // course 설정 안 한 경우 null
    val courseId = _courseId.asStateFlow()

    private val _goalPace = MutableStateFlow<Int?>(null) // 페이스 설정 안 한 경우 null
    val goalPace = _goalPace.asStateFlow()

    val runningRecord = repository.runningRecord
    val userLocation = repository.userLocation
    val time = repository.time
    val recordSize = repository.recordSize
    val trackingStatus = repository.trackingStatus

    fun setUserLocation(state: UserLocationState) {
        if (state is UserLocationState.Exist) {
            repository.setInitialUserLocation(state.locations.last())
        }
    }

    fun setTrackingStatus(status: TrackingStatus) {
        repository.setTrackingStatus(status)
    }

    fun setCourseId(id: String?) {
        _courseId.value = id
    }

    fun setGoalPace(pace: Int?) {
        _goalPace.value = pace
    }
}