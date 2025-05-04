package com.D107.runmate.watch.presentation.running

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _runningTime = MutableStateFlow(0L)
    val runningTime: StateFlow<Long> = _runningTime.asStateFlow()

    private val _formattedTime = MutableStateFlow("0:00:00")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    private var timerJob: Job? = null
    private var startTime = 0L
    private var pausedTime = 0L

    // ViewModel이 생성될 때 심박수 수집 시작
    init {
//        Log.d("sensor", "ViewModel init")
        viewModelScope.launch {
            startHeartRateMonitoringUseCase()
            collectHeartRate()
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
            startHeartRateMonitoringUseCase()
        }
    }

    // 심박수 측정 중단
    fun stopMonitoring() {
        viewModelScope.launch {
            stopHeartRateMonitoringUseCase()
        }
    }

    // ViewModel이 파괴될 때 측정 중단
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            stopHeartRateMonitoringUseCase()
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
    }

    fun pauseTimer() {
        timerJob?.cancel()
        pausedTime = _runningTime.value
    }

    fun resetTimer() {
        timerJob?.cancel()
        startTime = 0L
        pausedTime = 0L
        _runningTime.value = 0L
        _formattedTime.value = "0:00:00"
    }

}