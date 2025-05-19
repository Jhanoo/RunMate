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
import android.content.Context
import android.content.Intent
import com.D107.runmate.watch.data.repository.CadenceRepositoryImpl
import com.D107.runmate.watch.domain.repository.CadenceRepository
import com.D107.runmate.watch.domain.repository.GpxRepository
import com.D107.runmate.watch.domain.usecase.cadence.GetCadenceUseCase
import com.D107.runmate.watch.domain.usecase.gpx.CreateGpxFileUseCase
import com.D107.runmate.watch.presentation.data.TokenManager
import com.D107.runmate.watch.presentation.service.BluetoothService
import com.D107.runmate.watch.presentation.service.LocationTrackingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException


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
    private val distanceRepository: DistanceRepository,
    private val getCadenceUseCase: GetCadenceUseCase,
    private val cadenceRepository: CadenceRepository,
    private val bluetoothService: BluetoothService,
    private val gpxRepository: GpxRepository
) : ViewModel() {
    // 심박수
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    // 최대 심박수 저장 변수
    private val _maxHeartRate = MutableStateFlow(0)
    val maxHeartRate: StateFlow<Int> = _maxHeartRate.asStateFlow()

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

    // 페이스 기록 및 심박수 기록 저장 리스트
    private val paceRecords = mutableListOf<String>()
    private val heartRateRecords = mutableListOf<Int>()
    private var lastHeartRateRecordTime = 0L
    private var lastPaceRecordTime = 0L

    // 평균 페이스와 평균 심박수 계산 결과
    private val _avgPace = MutableStateFlow("--'--\"")
    val avgPace: StateFlow<String> = _avgPace.asStateFlow()

    private val _avgHeartRate = MutableStateFlow(0)
    val avgHeartRate: StateFlow<Int> = _avgHeartRate.asStateFlow()

    // GPX 파일 생성
    @Inject
    lateinit var createGpxFileUseCase: CreateGpxFileUseCase

    // 위치 추적 서비스가 실행 중인지 여부
    private val _isLocationTrackingActive = MutableStateFlow(false)
    val isLocationTrackingActive: StateFlow<Boolean> = _isLocationTrackingActive.asStateFlow()

    // 케이던스 관련 상태 추가
    private val _cadence = MutableStateFlow(0)
    val cadence: StateFlow<Int> = _cadence.asStateFlow()
    private var cadenceJob: Job? = null

    // 폰 연결
//    private val _isPhoneConnected = MutableStateFlow(false)
//    val isPhoneConnected: StateFlow<Boolean> = _isPhoneConnected.asStateFlow()

    // 시작여부 받기
//    private val _hasValidToken = MutableStateFlow(false)
//    val hasValidToken: StateFlow<Boolean> = _hasValidToken.asStateFlow()

    // 블루투스 연결 상태
    private val _bluetoothConnected = MutableStateFlow(false)
    val bluetoothConnected: StateFlow<Boolean> = _bluetoothConnected.asStateFlow()

    init {
//        Log.d("sensor", "ViewModel init")
        viewModelScope.launch {
            startHeartRateMonitoringUseCase()
            collectHeartRate()
            collectDistance() // 여기에 추가
            Log.d("Cadence", "ViewModel init: 케이던스 모니터링 시작")
        }

        viewModelScope.launch {
            bluetoothService.connectionState.collect { isConnected ->
                _bluetoothConnected.value = isConnected
            }
        }


        observeHeartRateAndPaceForTracking()
    }

    // 케이던스 수집 시작 메서드
    private fun startCadenceCollection() {
        // 기존 작업 취소
        cadenceJob?.cancel()

        // 케이던스 모니터링 시작
//        (cadenceRepository as? CadenceRepositoryImpl)?.startMonitoring()

        // 새 작업 시작
        cadenceJob = viewModelScope.launch {
            try {
                while(isActive) {
                    try {
                        val cadenceValue = getCadenceUseCase()
                        _cadence.value = cadenceValue
                        Log.d("Cadence", "캐이던스 업데이트: $cadenceValue SPM")
                    } catch (e: Exception) {
                        Log.e("Cadence", "캐이던스 값 가져오기 실패: ${e.message}")
                    }
                    delay(3000) // 3초마다 업데이트
                }
            } catch (e: CancellationException) {
                // 코루틴 취소는 정상적인 흐름으로 처리
                Log.d("Cadence", "캐이던스 수집 작업이 정상적으로 취소됨")
            } catch (e: Exception) {
                // 다른 예외는 에러로 로그
                Log.e("Cadence", "캐이던스 수집 중 예상치 못한 오류: ${e.message}")
            }
        }
        Log.d("Cadence", "케이던스 수집 시작됨")
    }

    // 케이던스 수집 중지 메서드
    private fun stopCadenceCollection() {
        cadenceJob?.cancel()
        cadenceJob = null
        (cadenceRepository as? CadenceRepositoryImpl)?.stopMonitoring()
        Log.d("Cadence", "케이던스 수집 중지됨")
    }



    // 위치 추적 서비스 시작
    fun startLocationTracking(context: Context) {
        Log.d("GpxTracking", "위치 추적 서비스 시작 요청")
        // 현재 심박수와 페이스 정보를 서비스에 전달
        LocationTrackingService.updateHeartRate(_heartRate.value)
        LocationTrackingService.updatePace(_currentPace.value)

        // 서비스 시작
        val intent = Intent(context, LocationTrackingService::class.java)
        intent.action = LocationTrackingService.ACTION_START
        context.startService(intent)
        _isLocationTrackingActive.value = true
//        Log.d("GpxTracking", "위치 추적 서비스 시작 요청 완료 - HR: ${_heartRate.value}, Pace: ${_currentPace.value}")
    }

    // 심박수 전송
    fun startHeartRateOnlyTracking(context: Context) {
        Log.d("HeartRateTracking", "심박수 전송 서비스 시작")
        LocationTrackingService.updateHeartRate(_heartRate.value)

        val intent = Intent(context, LocationTrackingService::class.java)
//        intent.action = LocationTrackingService.ACTION_START_HEART_RATE_ONLY
        context.startService(intent)
        _isLocationTrackingActive.value = true
    }

    // 위치 추적 서비스 일시 중지
    fun pauseLocationTracking(context: Context) {
        Log.d("GpxTracking", "위치 추적 서비스 일시 중지 요청")
        val intent = Intent(context, LocationTrackingService::class.java)
        intent.action = LocationTrackingService.ACTION_PAUSE
        context.startService(intent)
        _isLocationTrackingActive.value = false
    }

    // 위치 추적 서비스 재개
    fun resumeLocationTracking(context: Context) {
        Log.d("GpxTracking", "위치 추적 서비스 재개 요청")
        val intent = Intent(context, LocationTrackingService::class.java)
        intent.action = LocationTrackingService.ACTION_RESUME
        context.startService(intent)
        _isLocationTrackingActive.value = true
    }

    // 위치 추적 서비스 중지
    fun stopLocationTracking(context: Context) {
        Log.d("GpxTracking", "위치 추적 서비스 중지 요청")
        val intent = Intent(context, LocationTrackingService::class.java)
        intent.action = LocationTrackingService.ACTION_STOP
        context.startService(intent)
        _isLocationTrackingActive.value = false
    }

    // 심박수와 페이스 업데이트를 위한 Observe 설정
    private fun observeHeartRateAndPaceForTracking() {
        viewModelScope.launch {
            // 심박수 변경 감지 및 서비스에 전달
            heartRate.collect { bpm ->
                LocationTrackingService.updateHeartRate(bpm)
            }
        }

        viewModelScope.launch {
            // 페이스 변경 감지 및 서비스에 전달
            currentPace.collect { pace ->
                LocationTrackingService.updatePace(pace)
            }
        }

        viewModelScope.launch {
            cadence.collect { cadenceValue ->
                // 서비스에 캐이던스 업데이트 함수가 있다면 사용
                Log.d("Cadence", "캐이던스 현재값: $cadenceValue")
            }
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
                    val bpm = heartRate.bpm
                    _heartRate.value = bpm

                    // 최대 심박수 갱신
                    if (bpm > _maxHeartRate.value) {
                        _maxHeartRate.value = bpm
                    }

                    // 심박수 기록 추가 (5초마다)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastHeartRateRecordTime >= 5000) {
                        heartRateRecords.add(bpm)
                        lastHeartRateRecordTime = currentTime

                        // 평균 심박수 계산
                        if (heartRateRecords.isNotEmpty()) {
                            _avgHeartRate.value = heartRateRecords.sum() / heartRateRecords.size
                        }
                    }
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

                if (timeSeconds > 5 && distanceChange > 0.005) {
                    val newPace = calculatePaceUseCase(distanceChange, timeSeconds.toLong())
                    _currentPace.value = newPace

                    // 페이스 기록 추가 (5초마다)
                    val paceRecordTime = System.currentTimeMillis()
                    if (paceRecordTime - lastPaceRecordTime >= 5000 && newPace != "--'--\"") {
                        paceRecords.add(newPace)
                        lastPaceRecordTime = paceRecordTime

                        // 평균 페이스 계산
                        calculateAveragePace()
                    }
                } else {
                    _currentPace.value = "--'--\""
                }
            }
        }
    }

    // 평균 페이스 계산
    private fun calculateAveragePace() {
        if (paceRecords.isEmpty()) {
            _avgPace.value = "--'--\""
            return
        }

        // 페이스를 초로 변환하여 평균 계산
        var totalSeconds = 0L
        var validCount = 0

        paceRecords.forEach { pace ->
            if (pace != "--'--\"") {
                val parts = pace.split("'", "\"")
                if (parts.size >= 2) {
                    val minutes = parts[0].toIntOrNull() ?: 0
                    val seconds = parts[1].toIntOrNull() ?: 0
                    totalSeconds += (minutes * 60 + seconds)
                    validCount++
                }
            }
        }

        if (validCount > 0) {
            val avgSeconds = totalSeconds / validCount
            val avgMinutes = avgSeconds / 60
            val avgSecs = avgSeconds % 60
            _avgPace.value = String.format("%d'%02d\"", avgMinutes, avgSecs)
        } else {
            _avgPace.value = "--'--\""
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

        // 케이던스 수집 시작
        startCadenceCollection()

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

        // 케이던스 수집 중지
        stopCadenceCollection()

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
        _currentPace.value = "0'00\""
        _maxHeartRate.value = 0
        _avgHeartRate.value = 0
        _avgPace.value = "--'--\""
        lastPaceCalculationTime = 0L
        lastCalculatedDistance = 0.0
        lastHeartRateRecordTime = 0L
        lastPaceRecordTime = 0L
        recentDistances.clear()
        heartRateRecords.clear()
        paceRecords.clear()
        distanceRepository.resetDistance()
        stopCadenceCollection()
    }

    // 러닝 종료 시 GPX 파일 생성
    suspend fun createGpxFile(context: Context, runName: String): Result<Long> {
        // 위치 추적 서비스 중지
        stopLocationTracking(context)

        // 트랙 포인트에서 시작/종료 시간 및 평균 고도 계산
        val trackPoints = gpxRepository.getSessionTrackPoints()
        val startTime = trackPoints.minByOrNull { it.time }?.time ?: Date()
        val endTime = trackPoints.maxByOrNull { it.time }?.time ?: Date()
        val avgElevation = trackPoints.map { it.elevation }.average()

        // GPX 파일 생성
        return createGpxFileUseCase(
            runName = runName,
            totalDistance = _distance.value,
            totalTime = _runningTime.value,
            avgHeartRate = _avgHeartRate.value,
            maxHeartRate = _maxHeartRate.value,
            avgPace = _avgPace.value,
            avgCadence = _cadence.value,
            startTime = startTime,
            endTime = endTime,
            avgElevation = avgElevation
        )
    }

}