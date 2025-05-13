package com.D107.runmate.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.running.PersonalRunningInfo
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import com.D107.runmate.presentation.utils.LocationUtils.getPaceFromSpeed
import com.D107.runmate.presentation.utils.LocationUtils.trackingLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "MainViewModel"
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RunningTrackingRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {
    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled = _isVibrationEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _nickname = MutableStateFlow<String?>(null)
    val nickname: StateFlow<String?> = _nickname

    private val _profileImage = MutableStateFlow<String?>(null)
    val profileImage: StateFlow<String?> = _profileImage

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

//    private val _isRunning = MutableStateFlow(false)
//    val isRunning = _isRunning.asStateFlow()

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
}

//@HiltViewModel
//class MainViewModel @Inject constructor(): ViewModel() {
//    private val _userLocation = MutableStateFlow<UserLocationState>(UserLocationState.Initial)
//    val userLocation = _userLocation.asStateFlow()
//
//    private val _runningRecord = MutableStateFlow<RunningRecordState>(RunningRecordState.Initial)
//    val runningRecord = _runningRecord.asStateFlow()
//
//    private val _time = MutableStateFlow<Int>(0)
//    val time = _time.asStateFlow()
//
//    private val _recordSize = MutableStateFlow<Int>(0)
//    val recordSize = _recordSize.asStateFlow()
//
//    private val _isVibrationEnabled = MutableStateFlow(true)
//    val isVibrationEnabled = _isVibrationEnabled.asStateFlow()
//
//    private val _isSoundEnabled = MutableStateFlow(true)
//    val isSoundEnabled = _isSoundEnabled.asStateFlow()
//
//    var runningJob: RunningJobState = RunningJobState.Initial
//
//    private var timeTrackingJob: Job? = null
//
///*
//    러닝 시 추적할 데이터: 시간, 거리, 평균 페이스, 현재 페이스, 케이던스, BPM, 평균 고도, 위치
//    시간: 1초씩 증가시키는 방법 사용
//    거리: 이전 위치와 현재 위치의 distanceTo 메소드로 차이(m) 구해서 더하기
//    현재 페이스: Location의 location.speed(m/s) 이용해서 계산으로 구하기
//    평균 페이스: 거리(m) / 시간(s) 로 구하기
//    케이던스: Sensor.TYPE_STEP_DETECTOR 를 이용해서 구하기
//    BPM: 워치로부터 전달받아서 띄우기
//    고도: Location의 location.altitude를 저장
//    평균 고도: Location의 location.altitude를 List로 저장해두고 평균 계산 --> 한 번씩 파일에 쓰면서 리스트 비울 때 평균 값 가지고 있기
//    위치: Location의 location.longitude, location.latitude로 측정
//*/
//
//    fun startTimeTracking() {
//        if (timeTrackingJob?.isActive == true) return
//
//        timeTrackingJob = viewModelScope.launch {
//            while (isActive) {
//                delay(1000)
//                _time.value++
//            }
//        }
//    }
//
//    fun stopTimeTracking() {
//        timeTrackingJob?.cancel()
//        timeTrackingJob = null
//    }
//
//    fun startLocationTracking(context: Context) {
//        if (runningJob is RunningJobState.None || runningJob is RunningJobState.Initial) { // 초기 상태 or 멈춘 상태
//            val newJob = viewModelScope.launch {
//                trackingLocation(context)
//                    .catch { e ->
//                        runningJob = RunningJobState.Error(e.message ?: "Unknown error")
//                        Log.e("Location", "Error: ${e.message}", e)
//                    }
//                    .collect { location ->
//                        when (_userLocation.value) {
//                            is UserLocationState.Initial -> {
//                                val personalRunningInfo = PersonalRunningInfo(0f, location.speed, location.altitude, location.speed, location.altitude)
//                                val tmpList = listOf<PersonalRunningInfo>(personalRunningInfo)
//                                _runningRecord.value = RunningRecordState.Exist(tmpList)
//                                _userLocation.value = UserLocationState.Exist(listOf(location))
//                                Log.d(TAG, "startLocationTracking: ${getPaceFromSpeed(location.speed)}")
//                            }
//
//                            is UserLocationState.Exist -> {
//                                when(val record = _runningRecord.value) {
//                                    is RunningRecordState.Exist -> {
//                                        val distanceDiff : Float = location.distanceTo((_userLocation.value as UserLocationState.Exist).locations.last())
//                                        val distance = distanceDiff + record.runningRecords.last().distance
//                                        // speed를 가지고 있다가 표현할 때만, pace로 표현하기
//                                        val altitudeSum = record.runningRecords.last().altitude + location.altitude
//                                        val tmpList: List<PersonalRunningInfo> = record.runningRecords + PersonalRunningInfo(distance/1000, distance / time.value, location.altitude, location.speed, altitudeSum)
//                                        _runningRecord.value = RunningRecordState.Exist(tmpList)
//                                        _userLocation.value = UserLocationState.Exist((_userLocation.value as UserLocationState.Exist).locations + location)
//                                        Log.d(TAG, "startLocationTracking Exist: ${altitudeSum}  ${distance}")
//
////
//                                    }
//                                    is RunningRecordState.Initial -> {
//                                        val personalRunningInfo = PersonalRunningInfo(0f, location.speed, location.altitude, location.speed, location.altitude)
//                                        val tmpList = listOf<PersonalRunningInfo>(personalRunningInfo)
//                                        _runningRecord.value = RunningRecordState.Exist(tmpList)
//                                        _userLocation.value = UserLocationState.Exist(listOf(location))
//                                        Log.d(TAG, "startLocationTracking Initial: ${getPaceFromSpeed(location.speed)}")
//                                    }
//                                }
//                            }
//                        }
//                    }
//            }
//
//            newJob.invokeOnCompletion { cause ->
//                runningJob = when {
//                    cause is CancellationException -> RunningJobState.None
//                    cause != null -> RunningJobState.Error(cause.message ?: "Unknown error")
//                    else -> RunningJobState.None
//                }
//            }
//            runningJob = RunningJobState.Active(newJob)
//        }
//    }
//
//    fun stopRunningTracking() {
//        when (val currentState = runningJob) {
//            is RunningJobState.Active -> {
//                currentState.job.cancel()
//                runningJob = RunningJobState.None
//            }
//            else -> {
//                // 별다른 처리 x
//            }
//        }
//    }
//
//    fun startCadenceTracking(context: Context) {
//        if (runningJob is RunningJobState.None || runningJob is RunningJobState.Initial) {
//            CadenceTracker.startTracking(context)
//        }
//    }
//
//    fun stopCadenceTracking(context: Context) {
//        if (runningJob is RunningJobState.Active) {
//            CadenceTracker.stopTracking()
//        }
//    }
//
//    fun setUserLocation(state: UserLocationState) {
//        _userLocation.value = state
//    }
//
//    fun toggleVibrationEnabled() {
//        _isVibrationEnabled.value = !_isVibrationEnabled.value
//    }
//
//    fun toggleSoundEnabled() {
//        _isSoundEnabled.value = !_isSoundEnabled.value
//    }
//}
//
//sealed class UserLocationState {
//    object Initial: UserLocationState()
//    data class Exist(val locations: List<Location>): UserLocationState()
//}
//
//sealed class RunningRecordState {
//    object Initial: RunningRecordState()
//    data class Exist(val runningRecords: List<PersonalRunningInfo>): RunningRecordState()
//}
//
//sealed class RunningJobState {
//    object Initial : RunningJobState()
//    data class Active(val job: Job) : RunningJobState()
//    data class Error(val message: String): RunningJobState()
//    object None: RunningJobState()
//}
