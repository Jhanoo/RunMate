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
import com.D107.runmate.watch.domain.usecase.pace.CalculatePaceUseCase
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

    // 페이스
    private val _currentPace = MutableStateFlow("0'00")
    val currentPace: StateFlow<String> = _currentPace.asStateFlow()
    private var lastCalculatedDistance = 0.0 // 마지막으로 계산에 사용된 거리 값을 저장하는 변수
    private var lastPaceCalculationTime = 0L // 마지막으로 계산에 사용된 시간 저장
    private val CURRENT_PACE_WINDOW = 30000L // 30초 윈도우
    private var recentDistances = mutableListOf<Pair<Long, Double>>() // 시간과 거리 저장 (타임스탬프, 거리)

    @Inject
    lateinit var calculatePaceUseCase: CalculatePaceUseCase

    init {
//        Log.d("sensor", "ViewModel init")
        viewModelScope.launch {
            startHeartRateMonitoringUseCase()
            collectHeartRate()
            collectDistance() // 여기에 추가
            Log.d("pace", "ViewModel init: 거리 수집 시작")
        }
    }

    // 거리 계산
    private fun collectDistance() {
        viewModelScope.launch {
            try {
                getDistanceUseCase()
                    .collect { distance ->
                        val newDistance = distance.kilometers
                        _distance.value = newDistance

                        // 거리 기반 페이스 계산 제거 (주기적 계산으로 대체)
                        if (newDistance > 0) {
                            Log.d("distance", "거리 업데이트: ${newDistance}km")
                            // 여기서 calculateCurrentPace() 호출 제거
                        }
                    }
            } catch (e: Exception) {
                Log.e("distance", "거리 수집 오류: ${e.message}")
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

    // 페이스 계산
    private fun calculateCurrentPace() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val currentDistance = _distance.value

            // 가장 오래된 데이터가 너무 오래되었으면 제거 (한번에 필터링)
            val validTime = currentTime - CURRENT_PACE_WINDOW
            recentDistances.add(Pair(currentTime, currentDistance))
            recentDistances.removeAll { it.first < validTime }

            if (recentDistances.size >= 2) {
                val oldest = recentDistances.first()
                val newest = recentDistances.last()

                val timeSeconds = (newest.first - oldest.first) / 1000.0
                val distanceChange = newest.second - oldest.second

                // 계산 로직 단순화
                if (timeSeconds > 5 && distanceChange > 0.005) {
                    _currentPace.value = calculatePaceUseCase(distanceChange, timeSeconds.toLong())
                    Log.d("pace", "계산됨: 시간=${timeSeconds}초, 거리=${distanceChange}km, 페이스=${_currentPace.value}")
                } else {
                    _currentPace.value = "--'--\""
                }
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
                    collectDistance() // 추가
                    isDistanceMonitoring = true
                    Log.d("pace", "거리 모니터링 및 수집 시작")
                }
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

        // 페이스 계산 Job 변수 추가
        val paceJob = viewModelScope.launch {
            while(true) {
                kotlinx.coroutines.delay(3000) // 3초마다 업데이트
                if (timerJob?.isActive != true) break
                calculateCurrentPace()
            }
        }

        timerJob = viewModelScope.launch {
            startTimerUseCase(startTime, pausedTime) { running ->
                _runningTime.value = running
            }.collect { running ->
                _runningTime.value = running
                _formattedTime.value = formatTimeUseCase(running)
            }

            // 타이머 종료되면 페이스 계산도 중지
            paceJob.cancel()
        }

        // 나머지 코드는 그대로 유지
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }

        // 거리 측정 시작 및 데이터 초기화
        viewModelScope.launch {
            if (!isDistanceMonitoring) {
                recentDistances.clear() // 페이스 데이터 초기화
                startDistanceMonitoringUseCase()
                isDistanceMonitoring = true
                collectDistance()
            }
        }
    }

    fun pauseTimer() {
        Log.d("pace", "타이머 일시정지: 페이스 계산 중지")

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
        _currentPace.value = "0'00\"" // 페이스 명시적 리셋 추가
        lastPaceCalculationTime = 0L
        lastCalculatedDistance = 0.0 // 마지막 계산 거리 초기화
        recentDistances.clear()
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