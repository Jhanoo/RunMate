package com.D107.runmate.presentation

import androidx.lifecycle.ViewModel
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.D107.runmate.presentation.utils.SourceScreen
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TAG = "MainViewModel"
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RunningTrackingRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {
    private val _course = MutableStateFlow(Pair<String?, String?>(null, null)) // course 설정 안 한 경우 null, first: courseId, second: gpxFile
    val course = _course.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _nickname = MutableStateFlow<String?>(null)
    val nickname: StateFlow<String?> = _nickname

    private val _profileImage = MutableStateFlow<String?>(null)
    val profileImage: StateFlow<String?> = _profileImage

    private val _sourceScreen = MutableStateFlow(SourceScreen.RUNNING_FRAGMENT)
    val sourceScreen: StateFlow<String> = _sourceScreen.asStateFlow()

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    init {
        // DataStore에서 사용자 정보 로드
        viewModelScope.launch {
            dataStoreRepository.userId.collect { userId ->
                Timber.d("userID $userId")
                _userId.value = userId
            }
        }

        viewModelScope.launch {
            dataStoreRepository.nickname.collect { nickname ->
                _nickname.value = nickname
            }
        }

        viewModelScope.launch {
            dataStoreRepository.profileImage.collect { profileImage ->
                _profileImage.value = profileImage
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreRepository.clearAll()
        }
    }

    fun saveFcmToken(token: String) {
        _fcmToken.value = token
        viewModelScope.launch {
            dataStoreRepository.saveFcmToken(token)
        }
    }

    val runningRecord = repository.runningRecord
    val userLocation = repository.userLocation
    val time = repository.time
    val recordSize = repository.recordSize
    val trackingStatus = repository.trackingStatus
    val historyId = repository.historyId
    val courseId = repository.courseId
    val goalPace = repository.goalPace
    val isSound = repository.isSound
    val isVibration = repository.isVibration

    fun setUserLocation(state: UserLocationState) {
        if (state is UserLocationState.Exist) {
            repository.setInitialUserLocation(state.locations.last())
        }
    }

    fun setTrackingStatus(status: TrackingStatus) {
        repository.setTrackingStatus(status)
    }

    fun resetHistoryId() {
        repository.setHistoryId(null)
    }

    fun setCourse(courseId: String?, gpxFile: String?) {
        _course.value = Pair(courseId, gpxFile)
        repository.setCourseId(courseId)
    }

    fun setGoalPace(pace: Int?) {
//        _goalPace.value = pace
        repository.setGoalPace(pace ?: 0)
    }

    fun setSourceScreen(sourceScreen: String) {
        _sourceScreen.value = sourceScreen

    }
}