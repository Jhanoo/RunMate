package com.D107.runmate.presentation.wearable.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.Insole.CombinedInsoleData
import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.model.Insole.InsoleSide
import com.D107.runmate.domain.model.Insole.SmartInsole
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.usecase.smartInsole.ConnectInsoleUseCase
import com.D107.runmate.domain.usecase.smartInsole.DisconnectInsoleUseCase
import com.D107.runmate.domain.usecase.smartInsole.ObserveConnectionStateUseCase
import com.D107.runmate.domain.usecase.smartInsole.ObserveInsoleDataUseCase
import com.D107.runmate.domain.usecase.smartInsole.ScanInsoleUseCase
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InsoleViewModel @Inject constructor(
    // Use Case 주입 방식 (권장)
    private val scanInsolesUseCase: ScanInsoleUseCase,
    private val connectInsolePairUseCase: ConnectInsoleUseCase,
    private val disconnectInsoleUseCase: DisconnectInsoleUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val observeInsoleDataUseCase: ObserveInsoleDataUseCase
) : ViewModel() {

    private val _scanState = MutableStateFlow(false) // true: 스캔 중, false: 스캔 안 함
    val scanState: StateFlow<Boolean> = _scanState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<SmartInsole>>(emptyList())
    val scannedDevices: StateFlow<List<SmartInsole>> = _scannedDevices.asStateFlow()

    // --- 선택된 인솔 관리 ---
    private val _selectedLeftInsole = MutableStateFlow<SmartInsole?>(null)
    val selectedLeftInsole: StateFlow<SmartInsole?> = _selectedLeftInsole.asStateFlow()

    private val _selectedRightInsole = MutableStateFlow<SmartInsole?>(null)
    val selectedRightInsole: StateFlow<SmartInsole?> = _selectedRightInsole.asStateFlow()

    // 양쪽 인솔이 모두 선택되었는지 여부 (UI에서 페어링 버튼 활성화 등에 사용)
    val isPairingReady: StateFlow<Boolean> = combine(
        _selectedLeftInsole, _selectedRightInsole
    ) { left, right ->
        left != null && right != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- 연결 및 데이터 ---
    private val _connectionState = MutableStateFlow(InsoleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<InsoleConnectionState> = _connectionState.asStateFlow()

    private val _combinedData = MutableStateFlow<CombinedInsoleData?>(null)
    val combinedData: StateFlow<CombinedInsoleData?> = _combinedData.asStateFlow()

    // --- 오류 메시지 ---
    private val _errorEvent = MutableSharedFlow<String>() // UI에 일회성 메시지 전달 (Snackbar 등)
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private var scanJob: Job? = null
    private var dataObservationJob: Job? = null

    init {
        // ViewModel 생성 시 연결 상태 관찰 시작
        observeConnectionState()
    }

    fun startScan() {
        // 이미 스캔 중이면 중복 실행 방지
        if (_scanState.value) return

        // 이전 스캔 결과 초기화 (선택 사항)
        _scannedDevices.value = emptyList()

        scanJob?.cancel() // 이전 Job 취소 (안전 장치)
        scanJob = viewModelScope.launch {
            _scanState.value = true
            try {
                // 스캔 시작 및 결과 수집
                scanInsolesUseCase() // Flow<List<SmartInsoleDevice>> 반환
                    .catch { e ->
                        Timber.e(e, "Scan Flow Exception")
                        _errorEvent.emit("스캔 중 오류 발생: ${e.message}")
                        _scanState.value = false // 스캔 종료 상태로 변경
                    }
                    .collect { devices ->
                        _scannedDevices.value = devices
                    }
            } finally {
                // Flow가 완료되거나 취소되면 항상 실행
                // stopScan() 호출은 보통 Dialog 닫힐 때 하므로 여기선 상태만 변경
                if (_scanState.value) { // 명시적으로 중지되지 않았다면 상태 변경
                    _scanState.value = false
                    Timber.d("Scan Job finished or cancelled, setting scanState to false")
                }
            }
        }

//        // 일정 시간 후 자동 스캔 종료 (선택 사항)
//        viewModelScope.launch {
//            delay(15000) // 15초 후
//            if (_scanState.value) {
//                Timber.d("스캔 시간 초과, 스캔 중지")
//                stopScan()
//            }
//        }
    }

    fun stopScan() {
        if (!_scanState.value && scanJob == null) return // 이미 중지 상태면 무시
        Timber.d("스캔 중지 요청")
        scanJob?.cancel()
        scanJob = null
        _scanState.value = false
    }

    fun selectDevice(device: SmartInsole) {
        Timber.d("Device selected: ${device.name} (${device.address}), Side: ${device.side}")
        when (device.side) {
            InsoleSide.LEFT -> {
                // 이미 선택된 오른쪽과 같은 주소인지 확인 (실수 방지)
                if (_selectedRightInsole.value?.address == device.address) {
                    viewModelScope.launch { _errorEvent.emit("이미 오른쪽으로 선택된 인솔입니다.") }
                    return
                }
                _selectedLeftInsole.value = device
            }
            InsoleSide.RIGHT -> {
                // 이미 선택된 왼쪽과 같은 주소인지 확인
                if (_selectedLeftInsole.value?.address == device.address) {
                    viewModelScope.launch { _errorEvent.emit("이미 왼쪽으로 선택된 인솔입니다.") }
                    return
                }
                _selectedRightInsole.value = device
            }
            InsoleSide.UNKNOWN -> {
                // 이름 규칙으로 좌/우 판별 실패 시 사용자에게 알림
                viewModelScope.launch { _errorEvent.emit("왼쪽/오른쪽 구분이 불가능한 인솔입니다. (이름 확인 필요)") }
                Timber.w("Cannot determine side for device: ${device.name}")
                // 필요 시, 사용자가 직접 지정하는 로직 추가 가능
            }
        }
    }

    fun clearSelection() {
        _selectedLeftInsole.value = null
        _selectedRightInsole.value = null
        Timber.d("인솔 선택 초기화됨")
    }

    fun pairSelectedDevices() {
        val left = _selectedLeftInsole.value
        val right = _selectedRightInsole.value

        if (left != null && right != null) {
            Timber.i("페어링 시작: Left=${left.address}, Right=${right.address}")
            // 연결 시도 전 스캔 중지
            stopScan()
            // 연결 상태 초기화 및 데이터 관찰 시작
            _connectionState.value = InsoleConnectionState.CONNECTING // UI 피드백 즉시 주기
            // Use Case 호출
            connectInsolePairUseCase(left.address, right.address)
            // 데이터 관찰 시작 (연결 성공 후 데이터 들어옴)
            observeCombinedData()
        } else {
            Timber.w("페어링 시도 실패: 양쪽 인솔이 선택되지 않음")
            viewModelScope.launch { _errorEvent.emit("왼쪽과 오른쪽 인솔을 모두 선택해주세요.") }
        }
    }

    fun disconnect() {
        Timber.i("연결 해제 시도")
        // Use Case 호출
        disconnectInsoleUseCase()
        // 데이터 관찰 중지
        stopDataObservation()
        // 선택 해제 (선택 사항)
        // clearSelection()
    }


    private fun observeConnectionState() {
        viewModelScope.launch {
            observeConnectionStateUseCase()
                .collect { state ->
                    _connectionState.value = state
                    Timber.d("Connection State Updated: $state")
                    // 연결 끊김 또는 실패 시 데이터 관찰 중지
                    if (state == InsoleConnectionState.DISCONNECTED || state == InsoleConnectionState.FAILED) {
                        stopDataObservation()
                        // 선택 해제 (선택 사항)
                        // clearSelection()
                    }
                    // 완전 연결 시 데이터 관찰 시작 (이미 시작했을 수 있지만 안전하게)
                    if (state == InsoleConnectionState.FULLY_CONNECTED) {
                        observeCombinedData()
                    }
                }
        }
    }

    private fun observeCombinedData() {
        // 이미 관찰 중이면 중복 실행 방지
        if (dataObservationJob != null && dataObservationJob!!.isActive) {
            return
        }
        Timber.d("데이터 관찰 시작...")
        dataObservationJob = viewModelScope.launch {
            observeInsoleDataUseCase() // Flow<ResponseStatus<CombinedInsoleData>> 반환
                .catch { e ->
                    Timber.e(e, "Data Observation Flow Exception")
                    _errorEvent.emit("데이터 수신 중 오류 발생: ${e.message}")
                    // 오류 발생 시 null 또는 이전 상태 유지? -> null로 초기화
                    _combinedData.value = null
                }
                .collect { status ->
                    when (status) {
                        is ResponseStatus.Success -> {
                            _combinedData.value = status.data
                            // Timber.v("Combined Data Received: L=${status.data.left != null}, R=${status.data.right != null}") // 너무 빈번하면 주석 처리
                        }
                        is ResponseStatus.Error -> {
                            Timber.e("Data Observation Error: Code=${status.error.code}, Msg=${status.error.message}")
                            _errorEvent.emit("데이터 오류: ${status.error.message} (${status.error.code})")
                            // 오류 발생 시 데이터를 어떻게 처리할지? (null 유지, 이전 값 유지 등)
                            _combinedData.value = null // 오류 시 데이터 클리어
                        }
                    }
                }
        }
    }

    private fun stopDataObservation() {
        if (dataObservationJob != null && dataObservationJob!!.isActive) {
            Timber.d("데이터 관찰 중지...")
            dataObservationJob?.cancel()
            dataObservationJob = null
            // 데이터 클리어 (선택 사항)
            _combinedData.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel Cleared")
        // ViewModel 소멸 시 연결 해제 및 리소스 정리
        stopScan()
        disconnect() // 연결 상태라면 해제
        // 명시적으로 Job 취소 (viewModelScope 가 자동으로 처리하지만 안전하게)
        scanJob?.cancel()
        dataObservationJob?.cancel()
    }
}