package com.D107.runmate.watch.presentation.running

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.watch.data.repository.DistanceRepositoryImpl
import com.D107.runmate.watch.domain.repository.DistanceRepository
import com.D107.runmate.watch.domain.usecase.distance.GetDistanceUseCase
import com.D107.runmate.watch.domain.usecase.distance.StartDistanceMonitoringUseCase
import com.D107.runmate.watch.domain.usecase.distance.StopDistanceMonitoringUseCase
import com.D107.runmate.watch.domain.usecase.heartRate.GetHeartRateUseCase
import com.D107.runmate.watch.domain.usecase.heartRate.StartHeartRateMonitoringUseCase
import com.D107.runmate.watch.domain.usecase.heartRate.StopHeartRateMonitoringUseCase
import com.D107.runmate.watch.domain.usecase.timer.FormatTimeUseCase
import com.D107.runmate.watch.domain.usecase.timer.StartTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunningViewModel @Inject constructor(
    private val getHeartRateUseCase: GetHeartRateUseCase,
    private val startHeartRateMonitoringUseCase: StartHeartRateMonitoringUseCase,
    private val stopHeartRateMonitoringUseCase: StopHeartRateMonitoringUseCase,
    private val startTimerUseCase: StartTimerUseCase,
    private val formatTimeUseCase: FormatTimeUseCase,
    private val getDistanceUseCase: GetDistanceUseCase,
    private val startDistanceMonitoringUseCase: StartDistanceMonitoringUseCase,
    private val stopDistanceMonitoringUseCase: StopDistanceMonitoringUseCase,
    private val distanceRepository: DistanceRepository
) : ViewModel() {
    // 심박수
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _runningTime = MutableStateFlow(0L)
    val runningTime: StateFlow<Long> = _runningTime.asStateFlow()

    // 타이머
    private val _formattedTime = MutableStateFlow("0:00:00")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    private var timerJob: Job? = null
    private var startTime = 0L
    private var pausedTime = 0L

    // 러닝 거리
    private val _distance = MutableStateFlow(0.0)
    val distance: StateFlow<Double> = _distance.asStateFlow()
    private var isDistanceMonitoring = false

    init {
//        Log.d("sensor", "ViewModel init")
        viewModelScope.launch {
            startHeartRateMonitoringUseCase()
//            startDistanceMonitoringUseCase()
            collectHeartRate()
//            collectDistance()
        }
    }

    private fun collectDistance() {
        viewModelScope.launch {
            try {
                getDistanceUseCase()
                    .collect { distance ->
                        _distance.value = distance.kilometers
                        Log.d("distance", "ViewModel received distance: %.4f km".format(distance.kilometers))
                    }
            } catch (e: Exception) {
                Log.e("distance", "Error collecting distance: ${e.message}")
            }
        }
    }

    // 심박수 Flow를 수집하여 StateFlow에 업데이트
    private fun collectHeartRate() {
        viewModelScope.launch {
            getHeartRateUseCase()
                .collect { heartRate ->
//                    Log.d("sensor","심박수 in ViewModel : ${heartRate.bpm}")
                    _heartRate.value = heartRate.bpm // 수집한 심박수 값 갱신
                }
        }
    }

    // 심박수 측정 시작
    fun startMonitoring() {
        viewModelScope.launch {
            try {
                startHeartRateMonitoringUseCase()
                if (!isDistanceMonitoring) {
                    startDistanceMonitoringUseCase()
                    isDistanceMonitoring = true
                }
                Log.d("distance", "Monitoring started")
            } catch (e: Exception) {
                Log.e("distance", "Error starting monitoring: ${e.message}")
            }
        }
    }

    // 심박수 측정 중단
    fun stopMonitoring() {
        viewModelScope.launch {
            stopHeartRateMonitoringUseCase()
            stopDistanceMonitoringUseCase()
            isDistanceMonitoring = false
        }
    }

    // ViewModel이 파괴될 때 측정 중단
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            stopHeartRateMonitoringUseCase()
            stopDistanceMonitoringUseCase()
            isDistanceMonitoring = false
        }
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            startTimerUseCase(startTime, pausedTime) { running ->
                _runningTime.value = running
            }.collect { running ->
                _runningTime.value = running
                _formattedTime.value = formatTimeUseCase(running)
            }
        }

        if(startTime == 0L) {
            startTime = System.currentTimeMillis()
        }

        // 거리 측정도 재시작
        viewModelScope.launch {
            if (!isDistanceMonitoring) {
                startDistanceMonitoringUseCase()
                isDistanceMonitoring = true
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        pausedTime = _runningTime.value

        // 거리 측정 일시정지
        viewModelScope.launch {
            stopDistanceMonitoringUseCase()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        startTime = 0L
        pausedTime = 0L
        _runningTime.value = 0L
        _formattedTime.value = "0:00:00"
        _distance.value = 0.0
        distanceRepository.resetDistance()
//        isDistanceMonitoring = false
//
//        // DistanceRepository의 resetDistance 직접 호출
//        viewModelScope.launch {
//            stopDistanceMonitoringUseCase()  // 먼저 모니터링 중지
//            // Repository에 직접 접근
//            (distanceRepository as? DistanceRepositoryImpl)?.resetDistance()
//        }
    }

}