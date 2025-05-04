package com.D107.runmate.presentation.wearable.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.Insole.CombinedInsoleData
import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.model.Insole.InsoleSide
import com.D107.runmate.domain.model.Insole.SmartInsole
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.usecase.smartInsole.ClearInsoleAddressesUseCase
import com.D107.runmate.domain.usecase.smartInsole.ConnectInsoleUseCase
import com.D107.runmate.domain.usecase.smartInsole.DisconnectInsoleUseCase
import com.D107.runmate.domain.usecase.smartInsole.GetSavedInsoleAddressesUseCase
import com.D107.runmate.domain.usecase.smartInsole.ObserveConnectionStateUseCase
import com.D107.runmate.domain.usecase.smartInsole.ObserveInsoleDataUseCase
import com.D107.runmate.domain.usecase.smartInsole.SaveConnectedInsoleAddressesUseCase
import com.D107.runmate.domain.usecase.smartInsole.ScanInsoleUseCase
import com.D107.runmate.presentation.wearable.state.InsoleCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val connectInsolePairUseCase: ConnectInsoleUseCase, // UseCase 이름 확인 (페어 연결 담당)
    private val disconnectInsoleUseCase: DisconnectInsoleUseCase, // UseCase 이름 확인 (단순 연결 해제)
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val observeInsoleDataUseCase: ObserveInsoleDataUseCase,
    private val saveConnectedInsoleAddressesUseCase: SaveConnectedInsoleAddressesUseCase,
    private val getSavedInsoleAddressesUseCase: GetSavedInsoleAddressesUseCase,
    private val clearInsoleAddressesUseCase: ClearInsoleAddressesUseCase
) : ViewModel() {

    // --- StateFlow 변수들 (기존과 동일) ---
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

    private val savedAddressesFlow: StateFlow<Pair<String?, String?>> =
        getSavedInsoleAddressesUseCase() // UseCase 호출
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    private val _isDeviceSavedState = MutableStateFlow(false)
    val isDeviceSavedState: StateFlow<Boolean> = _isDeviceSavedState.asStateFlow()
    private val _connectionState = MutableStateFlow(InsoleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<InsoleConnectionState> = _connectionState.asStateFlow()

    val insoleCardState: StateFlow<InsoleCardState> = combine(
        connectionState, // 실제 BLE 연결 상태
        _isDeviceSavedState // DataStore에 주소가 저장되어 있는지 여부
    ) { connState, isSaved ->
        determineCardState(connState, isSaved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsoleCardState.DISCONNECTED_NO_SAVED)

    private var autoConnectAttempted = false



    private val _combinedData = MutableStateFlow<CombinedInsoleData?>(null)
    val combinedData: StateFlow<CombinedInsoleData?> = _combinedData.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private var scanJob: Job? = null
    private var dataObservationJob: Job? = null

    init {
        observeConnectionState()
        observeSavedAddresses()
        attemptAutoConnect()
    }

    private fun attemptAutoConnect() {
        // 저장 상태 Flow를 사용하여 시도 (observeSavedAddresses 에서 호출됨)
        if (autoConnectAttempted) return
        autoConnectAttempted = true // 시도 플래그 설정 (중복 방지)

        viewModelScope.launch {
            val (leftAddress, rightAddress) = getSavedInsoleAddressesUseCase().first() // 저장된 주소 즉시 가져오기
            if (!leftAddress.isNullOrBlank() && !rightAddress.isNullOrBlank()) {
                // 이미 연결 중이거나 연결된 상태가 아니면 시도
                if (connectionState.value == InsoleConnectionState.DISCONNECTED || connectionState.value == InsoleConnectionState.FAILED) {
                    Timber.i("자동 연결 시도: L=$leftAddress, R=$rightAddress")
                    _connectionState.value = InsoleConnectionState.CONNECTING // UI 피드백
                    connectInsolePairUseCase(leftAddress, rightAddress)
                    observeCombinedData() // 데이터 관찰 시작
                } else {
                    Timber.d("이미 연결(시도) 중이므로 자동 연결 스킵: ${connectionState.value}")
                }
            } else {
                Timber.d("자동 연결 스킵: 저장된 주소 없음.")
                // 자동 연결 시도 후에도 저장된 주소가 없으면 autoConnectAttempted 는 true 가 됨
            }
        }
    }

    private fun observeSavedAddresses() {
        viewModelScope.launch {
            getSavedInsoleAddressesUseCase()
                .map{(left, right) ->
                    Timber.d("DataStore Read: L=$left, R=$right")
                    !left.isNullOrBlank() && !right.isNullOrBlank()
                } // 양쪽 다 있어야 true
                .distinctUntilChanged() // 변경 시에만 업데이트
                .collect { isSaved ->
                    Timber.d("Saved device state updated: $isSaved")
                    _isDeviceSavedState.value = isSaved
                    // 저장된 주소가 있으면 자동 연결 시도 (최초 1회)
                    if (isSaved && !autoConnectAttempted) { // autoConnectAttempted 플래그 사용
                        attemptAutoConnect()
                    }
                }
        }
    }



    // --- 스캔 관련 함수 (startScan, stopScan) - 기존과 동일 ---
    fun startScan() {
        if (_scanState.value) return
        _scannedDevices.value = emptyList()
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _scanState.value = true
            try {
                scanInsolesUseCase()
                    .catch { e ->
                        Timber.e(e, "Scan Flow Exception")
                        _errorEvent.emit("스캔 중 오류 발생: ${e.message}")
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
        // 스캔 자동 종료 로직은 필요 시 유지
    }

    fun stopScan() {
        if (!_scanState.value && scanJob == null) return
        Timber.d("스캔 중지 요청")
        scanJob?.cancel()
        scanJob = null
        _scanState.value = false
        // stopScanUseCase() // 필요 시 Repository에 알리는 UseCase 호출
    }

    // --- 기기 선택 관련 함수 (selectDevice, clearSelection) - 기존과 동일 ---
    fun selectDevice(device: SmartInsole) {
        Timber.d("Device selected: ${device.name} (${device.address}), Side: ${device.side}")
        when (device.side) {
            InsoleSide.LEFT -> {
                if (_selectedRightInsole.value?.address == device.address) {
                    viewModelScope.launch { _errorEvent.emit("이미 오른쪽으로 선택된 인솔입니다.") }
                    return
                }
                _selectedLeftInsole.value = device
            }
            InsoleSide.RIGHT -> {
                if (_selectedLeftInsole.value?.address == device.address) {
                    viewModelScope.launch { _errorEvent.emit("이미 왼쪽으로 선택된 인솔입니다.") }
                    return
                }
                _selectedRightInsole.value = device
            }
            InsoleSide.UNKNOWN -> {
                viewModelScope.launch { _errorEvent.emit("왼쪽/오른쪽 구분이 불가능한 인솔입니다. (이름 확인 필요)") }
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
            InsoleConnectionState.FULLY_CONNECTED,
            InsoleConnectionState.PARTIALLY_CONNECTED,
            InsoleConnectionState.CONNECTING -> InsoleCardState.DISCONNECTED_SAVED // 연결 중이거나 연결됨 -> 연결된 카드 표시
            InsoleConnectionState.DISCONNECTED,
            InsoleConnectionState.FAILED -> {
                if (isSaved) {
                    InsoleCardState.DISCONNECTED_SAVED // 연결 안됨 + 저장됨 -> 연결 끊김(저장됨) 카드 표시
                } else {
                    InsoleCardState.DISCONNECTED_NO_SAVED // 연결 안됨 + 저장 안됨 -> 초기 카드 표시
                }
            }
        }
    }

    /**
     * 사용자가 선택한 양쪽 인솔 페어링(연결)을 시도합니다.
     */
    fun pairSelectedDevices() {
        val left = _selectedLeftInsole.value
        val right = _selectedRightInsole.value

        if (left != null && right != null) {
            Timber.i("페어링 시작 요청: Left=${left.address}, Right=${right.address}")
            stopScan() // 스캔 중지
            _connectionState.value = InsoleConnectionState.CONNECTING // 즉시 상태 변경
            connectInsolePairUseCase(left.address, right.address) // 연결 '시도'
            // 데이터 관찰 시작은 observeConnectionState에서 처리
        } else {
            Timber.w("페어링 시도 실패: 양쪽 인솔이 선택되지 않음")
            viewModelScope.launch { _errorEvent.emit("왼쪽과 오른쪽 인솔을 모두 선택해주세요.") }
        }
    }

    /**
     * 현재 연결된 인솔 페어의 연결을 해제합니다. (저장된 주소는 유지)
     */
    fun disconnect() {
        if (_connectionState.value != InsoleConnectionState.DISCONNECTED &&
            _connectionState.value != InsoleConnectionState.FAILED) {
            Timber.i("Disconnect 요청")
            disconnectInsoleUseCase() // UseCase 호출 (단순 해제)
            // stopDataObservation() // observeConnectionState 에서 상태 변경 시 자동으로 호출됨
            // clearSelection() // 필요 시 선택 해제
        }
    }

    /**
     * BLE 연결 상태 변화를 감지하고 관련 로직(주소 저장, 데이터 관찰 시작/중지 등)을 처리합니다.
     */
        private fun observeConnectionState() {
            viewModelScope.launch {
                observeConnectionStateUseCase().collect { state ->
                        val previousState = _connectionState.value
                        _connectionState.value = state
                        Timber.d("Connection State Updated: $state (Previous: $previousState)")

                        // --- 상태별 처리 ---
                        when (state) {
                            InsoleConnectionState.FULLY_CONNECTED -> {
                                // --- 연결 성공 시 처리 ---
                                if (previousState != InsoleConnectionState.FULLY_CONNECTED) {
                                    val leftAddress = _selectedLeftInsole.value?.address ?: savedAddressesFlow.value.first // 자동 연결 시 저장된 주소 사용
                                    val rightAddress = _selectedRightInsole.value?.address ?: savedAddressesFlow.value.second
                                    Timber.d("FullyConnect! $leftAddress $rightAddress")
                                    if (!leftAddress.isNullOrBlank() && !rightAddress.isNullOrBlank()) {
                                        viewModelScope.launch {
                                            try {
                                                // UseCase 호출하여 저장
                                                saveConnectedInsoleAddressesUseCase(leftAddress, rightAddress)
                                                Timber.i(">>> 연결 성공! 주소 저장됨 (UseCase): L=$leftAddress, R=$rightAddress")
                                            } catch (e: Exception) {

                                            }
                                        }
                                    } else { /* ... 주소 없음 처리 ... */ }
                                    observeCombinedData()
                                }
                            }
                        InsoleConnectionState.PARTIALLY_CONNECTED -> {
                            // 한쪽만 연결된 경우 데이터 관찰 시작 (선택 사항)
                            if (previousState != InsoleConnectionState.PARTIALLY_CONNECTED) {
                                observeCombinedData()
                            }
                        }
                        InsoleConnectionState.DISCONNECTED,
                        InsoleConnectionState.FAILED -> {
                            // 연결 끊김 또는 실패 시 처리
                            if (previousState == InsoleConnectionState.FULLY_CONNECTED || previousState == InsoleConnectionState.PARTIALLY_CONNECTED) {
                                stopDataObservation() // 데이터 관찰 중지
                                Timber.i("연결 해제 또는 실패로 데이터 관찰 중지.")
                                // clearSelection() // 선택 해제는 필요 시
                            }
                        }
                        InsoleConnectionState.CONNECTING -> {
                            // 연결 중...
                        }
                    }
                }
        }
    }

    /**
     * 연결된 인솔로부터 실시간 데이터를 관찰합니다.
     */
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
                    _errorEvent.emit("데이터 수신 중 오류 발생: ${e.message}")
                    _combinedData.value = null
                }
                .collect { status -> // ResponseStatus 처리
                    when (status) {
                        is ResponseStatus.Success -> {
                            _combinedData.value = status.data // 데이터 업데이트
                        }
                        is ResponseStatus.Error -> {
                            Timber.e("Data Observation Error: Code=${status.error.code}, Msg=${status.error.message}")
                            _errorEvent.emit("데이터 오류: ${status.error.message} (${status.error.code})")
                            // 오류 시 데이터 null 처리 또는 이전 값 유지 등 정책 결정
                            _combinedData.value = null
                        }
                    }
                }
        }
    }

    /**
     * 데이터 관찰을 중지합니다.
     */
    private fun stopDataObservation() {
        if (dataObservationJob != null && dataObservationJob!!.isActive) {
            Timber.d("데이터 관찰 중지...")
            dataObservationJob?.cancel()
            dataObservationJob = null
            _combinedData.value = null // 데이터 클리어
        }
    }

    fun forgetDevice() {
        viewModelScope.launch {
            Timber.i("Forget device requested.")
            try {
                // 주소 삭제 UseCase 호출
                clearInsoleAddressesUseCase()
                // 연결도 해제
                disconnectInsoleUseCase()
                clearSelection()
                _errorEvent.emit("저장된 인솔 정보가 삭제되었습니다.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to forget device")
                _errorEvent.emit("기기 정보 삭제 중 오류 발생")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel Cleared")
        stopScan()
        disconnect() // 연결 해제 (주소 유지)
        scanJob?.cancel()
        dataObservationJob?.cancel()
    }
}


enum class DiagnosisState {
    IDLE,       // 진단 안 함
    WARMUP,     // 워밍업 중 (데이터 수집 X, 타이머 O)
    RUNNING,    // 진단 진행 중 (데이터 수집 O, 타이머 O)
    ANALYZING,  // 데이터 분석 중
    FINISHED,   // 진단 및 분석 완료
    ERROR       // 오류 발생
}