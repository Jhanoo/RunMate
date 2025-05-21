package com.D107.runmate.presentation.utils

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.smartinsole.CombinedInsoleData
import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.model.smartinsole.InsoleConnectionState
import com.D107.runmate.domain.model.smartinsole.InsoleSide
import com.D107.runmate.domain.model.smartinsole.SmartInsole
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
import com.D107.runmate.domain.util.AnalysisState // GaitAnalyzerUtil의 enum
import com.D107.runmate.domain.util.GaitAnalyzerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// ViewModel에서 사용하던 상태 Enum. 필요시 InsoleManager 전용으로 분리하거나 GaitAnalyzerUtil.AnalysisState와 매핑하여 사용 가능.
// 여기서는 ViewModel과 동일한 상태를 유지하기 위해 그대로 사용.
enum class ManagerAnalysisProcessState {
    IDLE,
    CALIBRATING,
    READY_TO_ANALYZE,
    ANALYZING,
    STOPPED,
    ERROR
}

@Singleton
class InsoleManager @Inject constructor(
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
) {
//    private val managerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
//
//    private val _scanState = MutableStateFlow(false)
//    val scanState: StateFlow<Boolean> = _scanState.asStateFlow()
//
//    private val _scannedDevices = MutableStateFlow<List<SmartInsole>>(emptyList())
//    val scannedDevices: StateFlow<List<SmartInsole>> = _scannedDevices.asStateFlow()
//
//    private val _selectedLeftInsole = MutableStateFlow<SmartInsole?>(null)
//    val selectedLeftInsole: StateFlow<SmartInsole?> = _selectedLeftInsole.asStateFlow()
//
//    private val _selectedRightInsole = MutableStateFlow<SmartInsole?>(null)
//    val selectedRightInsole: StateFlow<SmartInsole?> = _selectedRightInsole.asStateFlow()
//
//    val isPairingReady: StateFlow<Boolean> = combine(
//        _selectedLeftInsole, _selectedRightInsole
//    ) { left, right ->
//        left != null && right != null
//    }.stateIn(managerScope, SharingStarted.WhileSubscribed(5000L), false)
//
//    private val _connectionState = MutableStateFlow(InsoleConnectionState.DISCONNECTED)
//    val connectionState: StateFlow<InsoleConnectionState> = _connectionState.asStateFlow()
//
//    val savedAddressesFlow: StateFlow<Pair<String?, String?>> =
//        getSavedInsoleAddressesUseCase()
//            .stateIn(managerScope, SharingStarted.WhileSubscribed(5000L), Pair(null, null))
//
//    private val _isDeviceSavedState = MutableStateFlow(false)
//    val isDeviceSavedState: StateFlow<Boolean> = _isDeviceSavedState.asStateFlow()
//
//    private val _combinedData = MutableStateFlow<CombinedInsoleData?>(null)
//    val combinedData: StateFlow<CombinedInsoleData?> = _combinedData.asStateFlow()
//
//    private val _analysisProcessState = MutableStateFlow(ManagerAnalysisProcessState.IDLE)
//    val analysisProcessState: StateFlow<ManagerAnalysisProcessState> = _analysisProcessState.asStateFlow()
//
//    val realTimeAnalysisResult: StateFlow<GaitAnalysisResult> = gaitAnalyzerUtil.currentAnalysisResult
//
//
//
//
//    private val _elapsedTimeSeconds = MutableStateFlow(0L)
//    val elapsedTimeSeconds: StateFlow<Long> = _elapsedTimeSeconds.asStateFlow()
//
//    val calibrationDurationSeconds: Long
//        get() = gaitAnalyzerUtil.getCalibrationDurationMs() / 1000
//
//    private var autoConnectAttempted = false // 자동 연결 시도 여부 플래그
//    private var isAutoConnecting = false // 현재 자동 연결 진행 중 플래그
//
//    private var scanJob: Job? = null
//    private var dataObservationJob: Job? = null
//    private var autoConnectScanJob: Job? = null
//    private var timerJob: Job? = null
//    // analysisResultJob은 realTimeAnalysisResult가 gaitAnalyzerUtil 것을 직접 사용하므로 불필요
//
//    init {
//        observeInternalConnectionState()
//        observeSavedAddressesChanges()
//        attemptAutoConnect() // 초기 자동 연결 시도
//
//        // GaitAnalyzerUtil의 상태 변경을 ManagerAnalysisProcessState와 동기화 (선택적, 더 정교한 상태관리)
//        gaitAnalyzerUtil.currentMode
//            .onEach { state ->
//                Timber.d("GaitAnalyzerUtil mode changed: $state")
//                // 필요하다면 _analysisProcessState를 업데이트 하는 로직 추가
//                // 예: if (state == AnalysisState.IDLE && _analysisProcessState.value == ManagerAnalysisProcessState.CALIBRATING)
//                //        _analysisProcessState.value = ManagerAnalysisProcessState.READY_TO_ANALYZE
//            }.launchIn(managerScope)
//    }
//
//    fun startScan() {
//        if (_scanState.value) return
//        _scannedDevices.value = emptyList()
//        scanJob?.cancel()
//        disconnect() // 스캔 전 기존 연결 해제 (ViewModel 로직과 동일)
//        _scanState.value = true
//        scanJob = managerScope.launch {
//            try {
//                scanInsolesUseCase()
//                    .catch { e ->
//                        Timber.e(e, "Scan Flow Exception in Manager")
//                        _scanState.value = false
//                    }
//                    .collect { devices ->
//                        _scannedDevices.value = devices
//                    }
//            } finally {
//                // ViewModel과 동일하게, collect가 끝나거나 취소되면 scanState를 false로.
//                // 단, 이 Job이 현재 scanJob일때만 false로 변경 (중복 실행 방지)
//                if (scanJob == this.coroutineContext[Job] && _scanState.value) {
//                    _scanState.value = false
//                    Timber.d("Scan Job finished or cancelled in Manager, setting scanState to false")
//                }
//            }
//        }
//    }
//
//    fun stopScan() {
//        if (!_scanState.value && scanJob == null) return // 이미 중지된 상태
//        Timber.d("Stop scan requested in Manager")
//        scanJob?.cancel()
//        scanJob = null
//        _scanState.value = false // 명시적으로 false 설정
//    }
//
//
//    fun selectDevice(device: SmartInsole) {
//        Timber.d("Device selected in Manager: ${device.name} (${device.address}), Side: ${device.side}")
//        when (device.side) {
//            InsoleSide.LEFT -> {
//                if (_selectedRightInsole.value?.address == device.address) return
//                _selectedLeftInsole.value = device
//            }
//            InsoleSide.RIGHT -> {
//                if (_selectedLeftInsole.value?.address == device.address) return
//                _selectedRightInsole.value = device
//            }
//            InsoleSide.UNKNOWN -> {
//                Timber.w("Cannot determine side for device: ${device.name}")
//            }
//        }
//    }
//
//    fun clearSelection() {
//        _selectedLeftInsole.value = null
//        _selectedRightInsole.value = null
//        Timber.d("Insole selection cleared in Manager")
//    }
//
//    fun pairSelectedDevices() {
//        val left = _selectedLeftInsole.value
//        val right = _selectedRightInsole.value
//
//        if (left != null && right != null) {
//            Timber.i("Pairing requested in Manager: Left=${left.address}, Right=${right.address}")
//            stopScan()
//            _connectionState.value = InsoleConnectionState.CONNECTING // 연결 시도 상태로 변경
//            connectInsolePairUseCase(left.address, right.address)
//        } else {
//            Timber.w("Pairing attempt failed in Manager: Both insoles not selected")
//        }
//    }
//
//    fun disconnect() {
//        // 연결된 상태이거나 연결 중일 때만 해제 시도
//        if (_connectionState.value != InsoleConnectionState.DISCONNECTED &&
//            _connectionState.value != InsoleConnectionState.FAILED) {
//            Timber.i("Disconnect requested in Manager")
//            disconnectInsoleUseCase() // UseCase 호출
//            // 연결 상태는 observeConnectionStateUseCase를 통해 자동으로 업데이트될 것임
//        }
//    }
//
//    fun forgetDevice() {
//        managerScope.launch {
//            Timber.i("Forget device requested in Manager.")
//            try {
//                clearInsoleAddressesUseCase() // 저장된 주소 삭제
//                disconnect() // 현재 연결 해제 (내부적으로 disconnectInsoleUseCase 호출)
//                clearSelection() // 선택된 인솔 정보 초기화
//                _isDeviceSavedState.value = false // UI 즉각 반영 위함 (선택적, observeSavedAddressesChanges가 처리)
//            } catch (e: Exception) {
//                Timber.e(e, "Failed to forget device in Manager")
//            }
//        }
//    }
//
//    private fun observeInternalConnectionState() {
//        observeConnectionStateUseCase()
//            .onEach { state ->
//                val previousState = _connectionState.value
//                _connectionState.value = state
//                Timber.d("Manager: Connection State Updated: $state (Previous: $previousState)")
//
//                if (state != InsoleConnectionState.FULLY_CONNECTED && previousState == InsoleConnectionState.FULLY_CONNECTED) {
//                    // 연결 끊김 시 분석 중단 (결과 저장 안함)
//                    if (_analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING || _analysisProcessState.value == ManagerAnalysisProcessState.CALIBRATING) {
//                        stopRealTimeAnalysisInternal("Connection lost or failed", saveResult = false)
//                    }
//                }
//
//                when (state) {
//                    InsoleConnectionState.FULLY_CONNECTED -> {
//                        if (previousState != InsoleConnectionState.FULLY_CONNECTED) {
//                            // 선택된 주소 또는 저장된 주소로 주소 저장
//                            val leftAddress = _selectedLeftInsole.value?.address ?: savedAddressesFlow.value.first
//                            val rightAddress = _selectedRightInsole.value?.address ?: savedAddressesFlow.value.second
//                            if (!leftAddress.isNullOrBlank() && !rightAddress.isNullOrBlank()) {
//                                managerScope.launch {
//                                    try {
//                                        saveConnectedInsoleAddressesUseCase(leftAddress, rightAddress)
//                                        Timber.i("Manager: Addresses saved on full connection: L=$leftAddress, R=$rightAddress")
//                                    } catch (e: Exception) {
//                                        Timber.e(e, "Manager: Failed to save addresses on full connection")
//                                    }
//                                }
//                            }
//                            startDataObservation()
//                        }
//                        isAutoConnecting = false // 자동 연결 성공 시 플래그 리셋
//                        stopAutoConnectScan()    // 자동 연결 스캔 중지
//                    }
//                    InsoleConnectionState.PARTIALLY_CONNECTED -> {
//                        if (previousState != InsoleConnectionState.PARTIALLY_CONNECTED) {
//                            startDataObservation() // 부분 연결 시에도 데이터 관찰 시작
//                        }
//                    }
//                    InsoleConnectionState.DISCONNECTED, InsoleConnectionState.FAILED -> {
//                        if (isAutoConnecting) { // 자동 연결 시도 중 실패/해제된 경우
//                            // isAutoConnecting = false // 여기서 false로 하면 attemptAutoConnect가 다시 돌 수 있음. attemptAutoConnect 내부에서 관리.
//                        } else {
//                            // 일반적인 연결 해제/실패
//                        }
//                        if (previousState == InsoleConnectionState.FULLY_CONNECTED || previousState == InsoleConnectionState.PARTIALLY_CONNECTED) {
//                            stopDataObservation()
//                            Timber.i("Manager: Data observation stopped due to disconnection or failure.")
//                        }
//                        // 자동 연결 재시도 로직 (조건부)
//                        if(!isAutoConnecting) attemptAutoConnect() // 수동 연결 해제 후에도 자동 연결 시도
//                    }
//                    InsoleConnectionState.CONNECTING -> {
//                        // 연결 시도 중일 때는 자동 연결 스캔 중지
//                        stopAutoConnectScan()
//                    }
//                }
//            }.launchIn(managerScope)
//    }
//
//    private fun observeSavedAddressesChanges() {
//        savedAddressesFlow // 이미 StateFlow이므로 추가적인 map이나 distinctUntilChanged는 선택적
//            .map { (left, right) -> !left.isNullOrBlank() && !right.isNullOrBlank() }
//            .distinctUntilChanged()
//            .onEach { isSaved ->
//                Timber.d("Manager: Saved device state updated: $isSaved. AutoConnectAttempted: $autoConnectAttempted")
//                _isDeviceSavedState.value = isSaved
//                if (isSaved && !autoConnectAttempted && _connectionState.value != InsoleConnectionState.FULLY_CONNECTED && !isAutoConnecting) {
//                    // autoConnectAttempted는 초기 한번만 시도하게 할지, 아니면 저장상태 변경 시마다 시도할지 정책에 따라.
//                    // ViewModel에서는 init 시 한번만 시도했었음.
//                    // 여기서는 저장된 주소가 있고, 연결 안되어있고, 자동연결 중이 아니면 시도.
//                    // attemptAutoConnect() // 이 로직은 init과 connectionState 변경 시 호출됨. 중복 호출 주의.
//                }
//                if (!isSaved) { // 저장된 주소 없어지면 자동연결 시도 플래그 리셋
//                    autoConnectAttempted = false
//                }
//            }
//            .launchIn(managerScope)
//    }
//
//
//    fun attemptAutoConnect() {
//        if (_connectionState.value == InsoleConnectionState.FULLY_CONNECTED ||
//            _connectionState.value == InsoleConnectionState.CONNECTING ||
//            autoConnectScanJob?.isActive == true ||
//            isAutoConnecting) {
//            Timber.d("Manager: Auto-connect condition not met or already in progress. State: ${_connectionState.value}, ScanActive: ${autoConnectScanJob?.isActive}, AutoConnecting: $isAutoConnecting")
//            return
//        }
//
//        managerScope.launch {
//            val (leftAddress, rightAddress) = getSavedInsoleAddressesUseCase().first() // 현재 저장된 주소 가져오기
//            if (leftAddress.isNullOrBlank() || rightAddress.isNullOrBlank()) {
//                Timber.d("Manager: No saved addresses for auto-connect.")
//                autoConnectAttempted = true // 시도했으나 주소 없음
//                return@launch
//            }
//
//            Timber.i("Manager: Attempting auto-connect scan for L=$leftAddress, R=$rightAddress")
//            autoConnectAttempted = true // 스캔 시도 자체를 기록
//            isAutoConnecting = true     // 자동 연결 프로세스 시작 플래그
//
//            autoConnectScanJob?.cancel() // 이전 자동 연결 스캔 작업 취소
//            _scanState.value = true // 스캔 상태 UI 반영 (선택적)
//
//            autoConnectScanJob = launch {
//                try {
//                    scanInsolesUseCase()
//                        .collectLatest { devices -> // collectLatest: 새 스캔 결과 오면 이전 처리 중단
//                            // 자동 연결 중이고, 아직 완전 연결 안됐을 때만 처리
//                            if (isAutoConnecting && _connectionState.value != InsoleConnectionState.FULLY_CONNECTED) {
//                                val foundLeft = devices.any { it.address == leftAddress }
//                                val foundRight = devices.any { it.address == rightAddress }
//
//                                if (foundLeft && foundRight) {
//                                    Timber.i("Manager: Auto-connect: Both saved insoles found. Attempting connection.")
//                                    stopAutoConnectScan() // 스캔 중지 (연결 시도 전)
//                                    _connectionState.value = InsoleConnectionState.CONNECTING // ViewModel과 동일하게 상태 변경
//                                    connectInsolePairUseCase(leftAddress, rightAddress)
//                                    // 연결 성공 여부는 observeInternalConnectionState 에서 처리
//                                    // isAutoConnecting = false 는 연결 성공/실패 시 변경
//                                }
//                            } else if (_connectionState.value == InsoleConnectionState.FULLY_CONNECTED) {
//                                Timber.d("Manager: Auto-connect scan detected full connection. Stopping scan.")
//                                stopAutoConnectScan()
//                                isAutoConnecting = false
//                            }
//                        }
//                } catch (e: Exception) {
//                    Timber.e(e, "Manager: Error during auto-connect scan.")
//                    isAutoConnecting = false
//                    _scanState.value = false
//                } finally {
//                    Timber.d("Manager: Auto-connect scan flow ended or cancelled.")
//                    if (autoConnectScanJob == coroutineContext[Job] && _scanState.value) { // 현재 job이고 scanState가 true일때만
//                        _scanState.value = false
//                    }
//                    // isAutoConnecting = false // 여기서 false로 하면 연결 시도 중 스캔이 재시작될 수 있음. 연결 결과에 따라 변경.
//                }
//            }
//        }
//    }
//
//    private fun stopAutoConnectScan() {
//        if (autoConnectScanJob?.isActive == true) {
//            Timber.d("Manager: Stopping auto-connect scan.")
//            autoConnectScanJob?.cancel()
//            autoConnectScanJob = null
//            if (_scanState.value) { // 스캔 상태가 자동 연결 스캔 때문이었다면 false로
//                // _scanState.value = false // 일반 스캔과 겹칠 수 있으므로 주의해서 관리
//            }
//        }
//    }
//
//
//    private fun startDataObservation() {
//        if (dataObservationJob?.isActive == true) {
//            Timber.d("Manager: Data observation already active.")
//            return
//        }
//        Timber.d("Manager: Starting data observation...")
//        dataObservationJob = observeInsoleDataUseCase()
//            .onEach { status ->
//                when (status) {
//                    is ResponseStatus.Success -> {
//                        _combinedData.value = status.data
//                        if (_analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING ||
//                            _analysisProcessState.value == ManagerAnalysisProcessState.CALIBRATING) {
//                            gaitAnalyzerUtil.processData(status.data)
//                        }
//                    }
//                    is ResponseStatus.Error -> {
//                        Timber.e("Manager: Data Observation Error: Code=${status.error.code}, Msg=${status.error.message}")
//                        _combinedData.value = null // 데이터 null 처리
//                        if (_analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING) {
//                            _analysisProcessState.value = ManagerAnalysisProcessState.ERROR
//                            timerJob?.cancel()
//                        }
//                    }
//                }
//            }
//            .catch { e ->
//                Timber.e(e, "Manager: Data Observation Flow Exception")
//                _combinedData.value = null
//                if (_analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING) {
//                    _analysisProcessState.value = ManagerAnalysisProcessState.ERROR
//                    timerJob?.cancel()
//                }
//            }
//            .launchIn(managerScope)
//    }
//
//    private fun stopDataObservation() {
//        if (dataObservationJob?.isActive == true) {
//            Timber.d("Manager: Stopping data observation...")
//            dataObservationJob?.cancel()
//            dataObservationJob = null
//            _combinedData.value = null
//        }
//    }
//
//    fun initiateTimedDiagnosis(durationSeconds: Long) {
//        if (_connectionState.value != InsoleConnectionState.FULLY_CONNECTED) {
//            Timber.w("Manager: Cannot start diagnosis, insoles not fully connected.")
//            // UI에 알림을 주기 위해 ViewModel에서 이 상태를 확인하고 Toast 등을 표시할 수 있음
//            return
//        }
//        if (_analysisProcessState.value == ManagerAnalysisProcessState.CALIBRATING || _analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING) {
//            Timber.w("Manager: Calibration or analysis already in progress.")
//            return
//        }
//
//        managerScope.launch {
//            _analysisProcessState.value = ManagerAnalysisProcessState.CALIBRATING
//            gaitAnalyzerUtil.reset() // 분석기 초기화
//
//            if (gaitAnalyzerUtil.startCalibration()) {
//                Timber.d("Manager: Calibration started. Waiting for ${gaitAnalyzerUtil.getCalibrationDurationMs()}ms")
//                // ViewModel의 delay 대신 GaitAnalyzerUtil의 상태를 관찰하는 것이 더 좋음.
//                // 여기서는 일단 ViewModel 로직과 유사하게 delay 사용.
//                delay(gaitAnalyzerUtil.getCalibrationDurationMs() + 500L) // 캘리브레이션 완료 대기 (약간의 버퍼)
//
//                if (gaitAnalyzerUtil.isCalibrated) {
//                    _analysisProcessState.value = ManagerAnalysisProcessState.READY_TO_ANALYZE
//                    startGaitAnalysisInternal(durationSeconds)
//                } else {
//                    Timber.e("Manager: Calibration failed.")
//                    _analysisProcessState.value = ManagerAnalysisProcessState.ERROR
//                }
//            } else {
//                Timber.e("Manager: Failed to start calibration.")
//                _analysisProcessState.value = ManagerAnalysisProcessState.ERROR
//            }
//        }
//    }
//
//    private fun startGaitAnalysisInternal(time: Long) { // durationSeconds
//        if (gaitAnalyzerUtil.startAnalysis()) {
//            _analysisProcessState.value = ManagerAnalysisProcessState.ANALYZING
//            Timber.i("Manager: Real-time gait analysis started (after calibration). Duration: $time seconds")
//
//            if (time != 0L) { // 0이 아니면 시간제한 있는 분석
//                timerJob?.cancel()
//                _elapsedTimeSeconds.value = 0L
//                timerJob = managerScope.launch {
//                    while (_analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING && isActive) {
//                        delay(1000)
//                        if (_analysisProcessState.value == ManagerAnalysisProcessState.ANALYZING) { // 상태 재확인
//                            _elapsedTimeSeconds.value += 1
//                            if (_elapsedTimeSeconds.value >= time) {
//                                stopRealTimeAnalysisInternal(reason = "Time expired", saveResult = true)
//                                break // 타이머 루프 종료
//                            }
//                        } else {
//                            break // 분석 상태 아니면 루프 종료
//                        }
//                    }
//                }
//            }
//        } else {
//            Timber.e("Manager: GaitAnalyzerUtil.startAnalysis() failed.")
//            _analysisProcessState.value = ManagerAnalysisProcessState.ERROR
//        }
//    }
//
//    // ViewModel에서 호출될 public 메서드
//    fun stopRealTimeAnalysis(reason: String = "User request", saveResult: Boolean = true) {
//        stopRealTimeAnalysisInternal(reason, saveResult)
//    }
//
//
//    private fun stopRealTimeAnalysisInternal(reason: String = "User request", saveResult: Boolean = true) {
//        val currentState = _analysisProcessState.value
//        // 분석 중이거나 캘리브레이션 중일 때만 중지 로직 수행
//        if (currentState != ManagerAnalysisProcessState.ANALYZING && currentState != ManagerAnalysisProcessState.CALIBRATING) {
//            Timber.d("Manager: Stop analysis called but not in analyzing/calibrating state. Current: $currentState")
//            return
//        }
//
//        Timber.i("Manager: Real-time analysis stop requested: $reason. Current state: $currentState, Save result: $saveResult")
//        _analysisProcessState.value = ManagerAnalysisProcessState.STOPPED
//        timerJob?.cancel()
//        gaitAnalyzerUtil.stopAnalysis() // GaitAnalyzerUtil에 중지 알림
//
//        if (saveResult && currentState == ManagerAnalysisProcessState.ANALYZING) {
//            // GaitAnalyzerUtil의 최신 결과 사용 (stopAnalysis() 호출 시 timestamp 갱신됨)
//            val finalResult = gaitAnalyzerUtil.currentAnalysisResult.value
//            Timber.d("Manager: Final result to save: $finalResult")
//            if (finalResult.totalLeftSteps > 0 || finalResult.totalRightSteps > 0) {
//                managerScope.launch {
//                    try {
//                        saveGaitAnalysisResultUseCase(finalResult)
//                        Timber.i("Manager: Final analysis result saved successfully.")
//                    } catch (e: Exception) {
//                        Timber.e(e, "Manager: Failed to save final analysis result.")
//                    }
//                }
//            } else {
//                Timber.w("Manager: No steps analyzed, result not saved.")
//            }
//        }
//        _elapsedTimeSeconds.value = 0L // 타이머 초기화는 resetAnalysis에서 하는게 더 일관성 있을수도. 여기선 중지 시 초기화.
//    }
//
//    fun resetAnalysis() {
//        Timber.i("Manager: Full analysis reset requested.")
//        // 실행 중인 분석/캘리브레이션이 있다면 먼저 중지 (결과 저장 안 함)
//        stopRealTimeAnalysisInternal("Reset request", saveResult = false)
//
//        gaitAnalyzerUtil.reset() // Analyzer 내부 상태 및 결과 초기화
//        _analysisProcessState.value = ManagerAnalysisProcessState.IDLE
//        _elapsedTimeSeconds.value = 0L
//        _combinedData.value = null // 이전 데이터 클리어
//        // 선택된 인솔이나 연결 상태는 유지. 필요시 clearSelection() 등 호출.
//    }
//
//    // 앱 종료 또는 Manager가 더 이상 필요 없을 때 호출 (Application 클래스 등에서)
//    fun clear() {
//        Timber.d("InsoleManager clear called. Cancelling managerScope.")
//        stopDataObservation()
//        stopScan()
//        stopAutoConnectScan()
//        timerJob?.cancel()
//        disconnectInsoleUseCase() // 활성 연결 해제
//        managerScope.cancel() // 모든 자식 코루틴 취소
//        gaitAnalyzerUtil.reset() // 유틸리티 상태도 초기화
//    }
}