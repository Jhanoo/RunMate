package com.D107.runmate.presentation.wearable.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.smartinsole.CombinedInsoleData
import com.D107.runmate.domain.model.smartinsole.InsoleConnectionState
import com.D107.runmate.domain.model.smartinsole.InsoleSide
import com.D107.runmate.domain.model.smartinsole.SmartInsole
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.usecase.smartinsole.ClearInsoleAddressesUseCase
import com.D107.runmate.domain.usecase.smartinsole.ConnectInsoleUseCase
import com.D107.runmate.domain.usecase.smartinsole.DisconnectInsoleUseCase
import com.D107.runmate.domain.usecase.smartinsole.GetSavedGaitAnalysisResultUseCase
import com.D107.runmate.domain.usecase.smartinsole.GetSavedInsoleAddressesUseCase
import com.D107.runmate.domain.usecase.smartinsole.ObserveConnectionStateUseCase
import com.D107.runmate.domain.usecase.smartinsole.ObserveInsoleDataUseCase
import com.D107.runmate.domain.usecase.smartinsole.SaveConnectedInsoleAddressesUseCase
import com.D107.runmate.domain.usecase.smartinsole.SaveGaitAnalysisResultUseCase
import com.D107.runmate.domain.usecase.smartinsole.ScanInsoleUseCase
import com.D107.runmate.domain.util.GaitAnalyzerUtil
import com.D107.runmate.presentation.wearable.state.InsoleCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InsoleViewModel @Inject constructor(
    private val scanInsolesUseCase: ScanInsoleUseCase,
    private val connectInsolePairUseCase: ConnectInsoleUseCase,
    private val disconnectInsoleUseCase: DisconnectInsoleUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val observeInsoleDataUseCase: ObserveInsoleDataUseCase,
    private val saveConnectedInsoleAddressesUseCase: SaveConnectedInsoleAddressesUseCase,
    private val getSavedInsoleAddressesUseCase: GetSavedInsoleAddressesUseCase,
    private val clearInsoleAddressesUseCase: ClearInsoleAddressesUseCase,
    private val gaitAnalyzerUtil: GaitAnalyzerUtil,
    private val saveGaitAnalysisResultUseCase: SaveGaitAnalysisResultUseCase,
    private val getSavedGaitAnalysisResultUseCase: GetSavedGaitAnalysisResultUseCase

) : ViewModel() {


    private val _scanState = MutableStateFlow(false)
    val scanState: StateFlow<Boolean> = _scanState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<SmartInsole>>(emptyList())
    val scannedDevices: StateFlow<List<SmartInsole>> = _scannedDevices.asStateFlow()

    private val _selectedLeftInsole = MutableStateFlow<SmartInsole?>(null)
    val selectedLeftInsole: StateFlow<SmartInsole?> = _selectedLeftInsole.asStateFlow()

    private val _selectedRightInsole = MutableStateFlow<SmartInsole?>(null)
    val selectedRightInsole: StateFlow<SmartInsole?> = _selectedRightInsole.asStateFlow()

    val isPairingReady: StateFlow<Boolean> = combine(
        _selectedLeftInsole, _selectedRightInsole
    ) { left, right ->
        left != null && right != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    //연결된 블루투스 주소
    private val savedAddressesFlow: StateFlow<Pair<String?, String?>> =
        getSavedInsoleAddressesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    val realTimeAnalysisResult =  MutableStateFlow(gaitAnalyzerUtil.currentAnalysisResult.value)

    private val _isDeviceSavedState = MutableStateFlow(false)
    val isDeviceSavedState: StateFlow<Boolean> = _isDeviceSavedState.asStateFlow()

    private val _connectionState = MutableStateFlow(InsoleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<InsoleConnectionState> = _connectionState.asStateFlow()
    //화면 상단 인솔 연결 상태 표시
    val insoleCardState: StateFlow<InsoleCardState> = combine(
        connectionState,
        isDeviceSavedState
    ) { connState, isSaved ->
        determineCardState(connState, isSaved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsoleCardState.DISCONNECTED_NO_SAVED)

    private val _combinedData = MutableStateFlow<CombinedInsoleData?>(null)
    val combinedData: StateFlow<CombinedInsoleData?> = _combinedData.asStateFlow()

    //진단하기 상태
    private val _analysisProcessState = MutableStateFlow(AnalysisProcessState.IDLE)
    val analysisProcessState: StateFlow<AnalysisProcessState> = _analysisProcessState.asStateFlow()

    //저장된 진단 결과
    private val _savedGaitAnalysisResult = MutableStateFlow<GaitAnalysisResult?>(null)
    val saveGaitAnalysisResult: StateFlow<GaitAnalysisResult?> = _savedGaitAnalysisResult.asStateFlow()

    //진단 시간 카운트
    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds: StateFlow<Long> = _elapsedTimeSeconds.asStateFlow()

    val calibrationDurationSeconds: Long
        get() = gaitAnalyzerUtil.getCalibrationDurationMs() / 1000

    //자동연결 중 플래그
    private var autoConnectAttempted = false

    private var analysisResultJob:Job? = null
    private var timerJob: Job? = null
    private var scanJob: Job? = null
    private var dataObservationJob: Job? = null
    private var autoConnectScanJob: Job? = null

    private var isAutoConnecting = false

    init {
        viewModelScope.launch {
            getSavedGaitAnalysisResultUseCase().collect(){
                _savedGaitAnalysisResult.value = it
            }
        }
        observeConnectionState()
        observeSavedAddresses()
        attemptAutoConnect()


    }

    //저장된 주소가 있으면 자동 재연결 시도
    private fun attemptAutoConnect() {
        if (_connectionState.value == InsoleConnectionState.FULLY_CONNECTED ||
            _connectionState.value == InsoleConnectionState.CONNECTING || // 이미 연결 시도 중일 때도 스캔 불필요
            autoConnectScanJob?.isActive == true ||
            isAutoConnecting) {
            Timber.d("자동 연결 스캔 시작 조건 미충족 또는 이미 진행 중. State: ${_connectionState.value}, ScanActive: ${autoConnectScanJob?.isActive}, AutoConnecting: $isAutoConnecting")
            return
        }

        viewModelScope.launch {
            val (leftAddress, rightAddress) = getSavedInsoleAddressesUseCase().first()
            if (leftAddress.isNullOrBlank() || rightAddress.isNullOrBlank()) {
                Timber.d("자동 연결 스캔: 저장된 주소 없음. 스캔 시작 안 함.")
                return@launch
            }

            Timber.i("자동 연결 스캔 시작: 찾을 주소 L=$leftAddress, R=$rightAddress")

            autoConnectScanJob?.cancel()

            autoConnectScanJob = launch {
                try {
                    scanInsolesUseCase()
                        .collect { devices ->
                            if (!isAutoConnecting && _connectionState.value != InsoleConnectionState.FULLY_CONNECTED) {
                                val foundLeft = devices.any { it.address == leftAddress }
                                val foundRight = devices.any { it.address == rightAddress }

                                if (foundLeft && foundRight) {
                                    Timber.i("자동 연결 스캔: 저장된 양쪽 인솔 발견! 연결 시도.")
                                    isAutoConnecting = true
                                    stopAutoConnectScan()
                                    _connectionState.value = InsoleConnectionState.CONNECTING
                                    connectInsolePairUseCase(leftAddress, rightAddress)
                                    observeCombinedData()
                                }
                            } else if (_connectionState.value == InsoleConnectionState.FULLY_CONNECTED) {
                                Timber.d("자동 연결 스캔 중 연결 감지. 스캔 중지.")
                                stopAutoConnectScan()
                            }
                        }
                } catch (e: Exception) {
                    Timber.e(e, "자동 연결 스캔 중 오류 발생")
                    // 오류 발생 시 스캔 상태 false로 변경
                    _scanState.value = false
                } finally {
                    // Flow가 완료되거나 취소되면 항상 실행
                    Timber.d("자동 연결 스캔 Flow 종료 또는 취소됨.")
                    if (_scanState.value && autoConnectScanJob == coroutineContext[Job]) { // 현재 Job이 맞는지 확인
                        _scanState.value = false
                    }
                }
            }
        }
    }

    private fun stopAutoConnectScan() {
        if (autoConnectScanJob?.isActive == true) {
            Timber.d("자동 연결 스캔 중지 요청.")
            autoConnectScanJob?.cancel()
            autoConnectScanJob = null
        }
    }

    private fun observeSavedAddresses() {
        viewModelScope.launch {
            getSavedInsoleAddressesUseCase()
                .map{(left, right) ->
                    Timber.d("DataStore Read: L=$left, R=$right")
                    !left.isNullOrBlank() && !right.isNullOrBlank()
                }
                .distinctUntilChanged()
                .collect { isSaved ->
                    Timber.d("Saved device state updated: $isSaved")
                    _isDeviceSavedState.value = isSaved
                    if (isSaved && !autoConnectAttempted) {
                        attemptAutoConnect()
                    }
                }
        }
    }

    //블루투스 기기 주소 data store에서 삭제
    fun forgetDevice() {
        viewModelScope.launch {
            Timber.i("Forget device requested.")
            try {
                clearInsoleAddressesUseCase()
                disconnectInsoleUseCase()
                clearSelection()
            } catch (e: Exception) {
                Timber.e(e, "Failed to forget device")
            }
        }
    }



    //디바이스 스캔 시작
    fun startScan() {
        if (_scanState.value) return
        _scannedDevices.value = emptyList()
        scanJob?.cancel()
        disconnect()
        scanJob = viewModelScope.launch {
            _scanState.value = true
            try {
                scanInsolesUseCase()
                    .catch { e ->
                        Timber.e(e, "Scan Flow Exception")
                        _scanState.value = false
                    }
                    .collect { devices ->
                        _scannedDevices.value = devices
                    }
            } finally {
                if (_scanState.value) {
                    _scanState.value = false
                    Timber.d("Scan Job finished or cancelled, setting scanState to false")
                }
            }
        }
    }

    //코루틴 취소로 스캔 중지
    fun stopScan() {
        if (!_scanState.value && scanJob == null) return
        Timber.d("스캔 중지 요청")
        scanJob?.cancel()
        scanJob = null
        _scanState.value = false
    }

    //디바이스 목록에서 인솔 디바이스 선택
    fun selectDevice(device: SmartInsole) {
        Timber.d("Device selected: ${device.name} (${device.address}), Side: ${device.side}")
        when (device.side) {
            InsoleSide.LEFT -> {
                if (_selectedRightInsole.value?.address == device.address) {
                    return
                }
                _selectedLeftInsole.value = device
            }
            InsoleSide.RIGHT -> {
                if (_selectedLeftInsole.value?.address == device.address) {
                    return
                }
                _selectedRightInsole.value = device
            }
            InsoleSide.UNKNOWN -> {
                Timber.w("Cannot determine side for device: ${device.name}")
            }
        }
    }

    fun clearSelection() {
        _selectedLeftInsole.value = null
        _selectedRightInsole.value = null
        Timber.d("인솔 선택 초기화됨")
    }

    private fun determineCardState(connState: InsoleConnectionState, isSaved: Boolean): InsoleCardState {
        return when (connState) {
            InsoleConnectionState.FULLY_CONNECTED -> InsoleCardState.CONNECTED
            InsoleConnectionState.PARTIALLY_CONNECTED,
            InsoleConnectionState.CONNECTING -> InsoleCardState.DISCONNECTED_SAVED
            InsoleConnectionState.DISCONNECTED,
            InsoleConnectionState.FAILED -> {
                if (isSaved) {
                    InsoleCardState.DISCONNECTED_SAVED
                } else {
                    InsoleCardState.DISCONNECTED_NO_SAVED
                }
            }
        }
    }


    fun pairSelectedDevices() {
        val left = _selectedLeftInsole.value
        val right = _selectedRightInsole.value

        if (left != null && right != null) {
            Timber.i("페어링 시작 요청: Left=${left.address}, Right=${right.address}")
            stopScan() // 스캔 중지
            _connectionState.value = InsoleConnectionState.CONNECTING
            connectInsolePairUseCase(left.address, right.address)
        } else {
            Timber.w("페어링 시도 실패: 양쪽 인솔이 선택되지 않음")
        }
    }


    fun disconnect() {
        if (_connectionState.value != InsoleConnectionState.DISCONNECTED &&
            _connectionState.value != InsoleConnectionState.FAILED) {
            Timber.i("Disconnect 요청")
            disconnectInsoleUseCase()
        }
    }

    //연결상태 확인
    private fun observeConnectionState() {
        viewModelScope.launch {
            observeConnectionStateUseCase().collect { state ->
                val previousState = _connectionState.value
                _connectionState.value = state
                Timber.d("Connection State Updated: $state (Previous: $previousState)")
                if (state != InsoleConnectionState.FULLY_CONNECTED&&previousState==InsoleConnectionState.FULLY_CONNECTED) {
                    stopRealTimeAnalysis("연결 끊김 또는 실패", saveResult = false)
                }
                when (state) {
                    InsoleConnectionState.FULLY_CONNECTED -> {
                        if (previousState != InsoleConnectionState.FULLY_CONNECTED) {
                            val leftAddress = _selectedLeftInsole.value?.address ?: savedAddressesFlow.value.first // 자동 연결 시 저장된 주소 사용
                            val rightAddress = _selectedRightInsole.value?.address ?: savedAddressesFlow.value.second
                            Timber.d("FullyConnect! $leftAddress $rightAddress")
                            if (!leftAddress.isNullOrBlank() && !rightAddress.isNullOrBlank()) {
                                viewModelScope.launch {
                                    try {
                                        saveConnectedInsoleAddressesUseCase(leftAddress, rightAddress)
                                        Timber.i(">>> 연결 성공! 주소 저장됨 (UseCase): L=$leftAddress, R=$rightAddress")
                                    } catch (e: Exception) {

                                    }
                                }
                            }
                            observeCombinedData()
                        }
                    }
                    InsoleConnectionState.PARTIALLY_CONNECTED -> {
                        if (previousState != InsoleConnectionState.PARTIALLY_CONNECTED) {
                            observeCombinedData()
                        }
                    }
                    InsoleConnectionState.DISCONNECTED,
                    InsoleConnectionState.FAILED -> {
                        isAutoConnecting = false
                        attemptAutoConnect()
                        if (previousState == InsoleConnectionState.FULLY_CONNECTED || previousState == InsoleConnectionState.PARTIALLY_CONNECTED) {
                            stopDataObservation()
                            Timber.i("연결 해제 또는 실패로 데이터 관찰 중지.")
                            // clearSelection() // 선택 해제는 필요 시
                        }
                    }
                    InsoleConnectionState.CONNECTING -> {
                        stopAutoConnectScan()
                    }
                }
            }
        }
    }

    //연결시 데이터 수신
    private fun observeCombinedData() {
        if (dataObservationJob != null && dataObservationJob!!.isActive) {
            Timber.d("Data observation already active.")
            return
        }
        Timber.d("데이터 관찰 시작...")
        dataObservationJob = viewModelScope.launch {
            observeInsoleDataUseCase()
                .catch { e -> // Flow 자체의 예외 처리
                    Timber.e(e, "Data Observation Flow Exception")
                    _combinedData.value = null
                    if (_analysisProcessState.value == AnalysisProcessState.ANALYZING) {
                        _analysisProcessState.value = AnalysisProcessState.ERROR
                        timerJob?.cancel()
                    }
                }
                .collect { status ->
                    when (status) {
                        is ResponseStatus.Success -> {
                            val data = status.data
                            _combinedData.value = data
//                            Timber.d("data : $data")
                            //진단중인경우 진단 유틸로 데이터 전달
                            if (_analysisProcessState.value == AnalysisProcessState.ANALYZING||_analysisProcessState.value == AnalysisProcessState.CALIBRATING) {
                                if(_analysisProcessState.value == AnalysisProcessState.ANALYZING) {
                                    analysisResultJob = launch {
                                        viewModelScope.launch {
                                            gaitAnalyzerUtil.currentAnalysisResult.collect { result ->
                                                realTimeAnalysisResult.value = result

                                            }
                                        }
                                    }
                                }
                                gaitAnalyzerUtil.processData(data)
                            }
                        }
                        is ResponseStatus.Error -> {
                            Timber.e("Data Observation Error: Code=${status.error.code}, Msg=${status.error.message}")
                            _combinedData.value = null
                            if (_analysisProcessState.value == AnalysisProcessState.ANALYZING) {
                                _analysisProcessState.value = AnalysisProcessState.ERROR
                                timerJob?.cancel()
                            }
                        }
                    }
                }
        }
    }

    //데이터 관찰 중지
    private fun stopDataObservation() {
        if (dataObservationJob != null && dataObservationJob!!.isActive) {
            Timber.d("데이터 관찰 중지...")
            dataObservationJob?.cancel()
            dataObservationJob = null
            _combinedData.value = null // 데이터 클리어
        }
    }


    // 진단 영점 조절, 시작
    fun initiateTimedDiagnosis(durationSeconds: Long) {
        if (_connectionState.value != InsoleConnectionState.FULLY_CONNECTED) {
            return
        }
        if (_analysisProcessState.value == AnalysisProcessState.CALIBRATING || _analysisProcessState.value == AnalysisProcessState.ANALYZING) {
            Timber.w("이미 캘리브레이션 또는 분석이 진행 중입니다.")
            return
        }

        viewModelScope.launch {
            _analysisProcessState.value = AnalysisProcessState.CALIBRATING
            gaitAnalyzerUtil.reset()
            //영점 조절 시작 후 시간 기다리고 시작
            if (gaitAnalyzerUtil.startCalibration()) {
                delay( gaitAnalyzerUtil.getCalibrationDurationMs() + 500L)//이건 다른 flow로 끝났다는 신호를 받고 시작하는게 낫긴 할듯

                if (gaitAnalyzerUtil.isCalibrated) {
                    _analysisProcessState.value = AnalysisProcessState.READY_TO_ANALYZE
                    startGaitAnalysis(durationSeconds)
                } else {
                    Timber.e("캘리브레이션 실패")
                    _analysisProcessState.value = AnalysisProcessState.ERROR
                }
            } else {
                Timber.e("캘리브레이션 시작 실패")
                _analysisProcessState.value = AnalysisProcessState.ERROR
            }
        }
    }

    // 진단 시작
    private fun startGaitAnalysis(time: Long) {
        if (gaitAnalyzerUtil.startAnalysis()) { // GaitAnalyzerUtil에 분석 시작 요청
            _analysisProcessState.value = AnalysisProcessState.ANALYZING
            Timber.i("실시간 보행 분석 시작 (캘리브레이션 완료 후)")

            if (time != 0L) {
                timerJob?.cancel()
                _elapsedTimeSeconds.value = 0L
                timerJob = viewModelScope.launch {
                    while (_analysisProcessState.value == AnalysisProcessState.ANALYZING) {
                        delay(1000)
                        if (_analysisProcessState.value == AnalysisProcessState.ANALYZING) { // 한번 더 체크
                            _elapsedTimeSeconds.value += 1
                            if (elapsedTimeSeconds.value >= time) {
                                stopRealTimeAnalysis(reason = "시간 만료", saveResult = true)
                                break
                            }
                        } else { break }
                    }
                }
            }
        } else {
            Timber.e("GaitAnalyzerUtil.startAnalysis() 실패")
            _analysisProcessState.value = AnalysisProcessState.ERROR
        }
    }


    //분석 중지, 완전히 분석한 것은 저장 플래그로 제어
    fun stopRealTimeAnalysis(reason: String = "사용자 요청", saveResult: Boolean = true) {
        val currentState = _analysisProcessState.value
        if (currentState != AnalysisProcessState.ANALYZING && currentState != AnalysisProcessState.CALIBRATING) {
            return
        }
        Timber.i("실시간 분석 중지 요청: $reason")
        _analysisProcessState.value = AnalysisProcessState.STOPPED // 상태 변경 (결과 보존)
        timerJob?.cancel() // 타이머 중지
        analysisResultJob?.cancel()
        gaitAnalyzerUtil.stopAnalysis()
        if (saveResult && currentState == AnalysisProcessState.ANALYZING) {
            val finalResult = realTimeAnalysisResult.value
            Timber.d("$finalResult")
            if (finalResult.totalLeftSteps > 0 || finalResult.totalRightSteps > 0) {
                viewModelScope.launch {
                    try {
                        saveGaitAnalysisResultUseCase(finalResult)
                        _savedGaitAnalysisResult.value = finalResult
                        Timber.i("최종 분석 결과 저장 성공 (via UseCase -> UserDataStoreSource).")
                    } catch (e: Exception) {
                        Timber.e(e, "최종 분석 결과 저장 실패")
                    }
                }
            } else {
                Timber.w("분석된 걸음이 없어 결과를 저장하지 않습니다.")
            }
        }
    }


    fun resetAnalysis() {
        Timber.i("분석 상태 완전 초기화")
        stopRealTimeAnalysis("리셋 요청", saveResult = false) // 실행 중이었다면 중지
        gaitAnalyzerUtil.reset() // Analyzer 내부 상태 및 결과 초기화
        _analysisProcessState.value = AnalysisProcessState.IDLE
        _elapsedTimeSeconds.value = 0L
    }


    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel Cleared")
        stopScan()
        stopAutoConnectScan()
        disconnect() // 연결 해제 (주소 유지)
        scanJob?.cancel()
        dataObservationJob?.cancel()
    }
}

enum class AnalysisProcessState {
    IDLE,
    CALIBRATING, // 캘리브레이션 진행 중
    READY_TO_ANALYZE, // 캘리브레이션 완료, 분석 시작 대기
    ANALYZING,
    STOPPED,
    ERROR
}
