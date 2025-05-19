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
import com.D107.runmate.presentation.utils.InsoleManager
import com.D107.runmate.presentation.utils.ManagerAnalysisProcessState
import com.D107.runmate.presentation.wearable.state.InsoleCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val insoleManager: InsoleManager,
    private val getSavedGaitAnalysisResultUseCase: GetSavedGaitAnalysisResultUseCase
) : ViewModel() {

    val scanState: StateFlow<Boolean> = insoleManager.scanState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val scannedDevices: StateFlow<List<SmartInsole>> = insoleManager.scannedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedLeftInsole: StateFlow<SmartInsole?> = insoleManager.selectedLeftInsole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedRightInsole: StateFlow<SmartInsole?> = insoleManager.selectedRightInsole
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isPairingReady: StateFlow<Boolean> = insoleManager.isPairingReady
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)



    val realTimeAnalysisResult: StateFlow<GaitAnalysisResult> = insoleManager.realTimeAnalysisResult
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),  GaitAnalysisResult(timestamp = 0L))

    val isDeviceSavedState: StateFlow<Boolean> = insoleManager.isDeviceSavedState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val connectionState: StateFlow<InsoleConnectionState> = insoleManager.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsoleConnectionState.DISCONNECTED)

    //화면 상단 인솔 연결 상태 표시 (이 로직은 ViewModel에 남겨둠)
    val insoleCardState: StateFlow<InsoleCardState> = combine(
        connectionState, // ViewModel의 connectionState 사용
        isDeviceSavedState // ViewModel의 isDeviceSavedState 사용
    ) { connState, isSaved ->
        determineCardState(connState, isSaved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsoleCardState.DISCONNECTED_NO_SAVED)


    val combinedData: StateFlow<CombinedInsoleData?> = insoleManager.combinedData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val analysisProcessState: StateFlow<ManagerAnalysisProcessState> =
        insoleManager.analysisProcessState
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ManagerAnalysisProcessState.IDLE)


    val _savedGaitAnalysisResult  = MutableStateFlow<GaitAnalysisResult?>(null)
    val savedGaitAnalysisResult = _savedGaitAnalysisResult.asStateFlow()

    val elapsedTimeSeconds: StateFlow<Long> = insoleManager.elapsedTimeSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val calibrationDurationSeconds: Long
        get() = insoleManager.calibrationDurationSeconds


    // ViewModel init에서 특별히 할 일은 없어짐. Manager가 자체적으로 초기화 로직 수행.
    init {
        viewModelScope.launch {
            getSavedGaitAnalysisResultUseCase().collect{
                Timber.d("savedGaitAnalysisResult $it")
                _savedGaitAnalysisResult.value = it
            }

        }

    }

    // --- Public 메서드들은 InsoleManager의 메서드를 호출 ---

    fun startScan() {
        insoleManager.startScan()
    }

    fun stopScan() {
        insoleManager.stopScan()
    }

    fun selectDevice(device: SmartInsole) {
        insoleManager.selectDevice(device)
    }

    fun clearSelection() {
        insoleManager.clearSelection()
    }

    fun pairSelectedDevices() {
        insoleManager.pairSelectedDevices()
    }

    fun disconnect() {
        insoleManager.disconnect()
    }

    fun forgetDevice() {
        insoleManager.forgetDevice()
    }

    fun initiateTimedDiagnosis(durationSeconds: Long) {
        insoleManager.initiateTimedDiagnosis(durationSeconds)
    }

    fun stopRealTimeAnalysis(reason: String = "사용자 요청", saveResult: Boolean = true) {
        insoleManager.stopRealTimeAnalysis(reason, saveResult)
    }

    fun resetAnalysis() {
        insoleManager.resetAnalysis()
    }

    // InsoleCardState 결정 로직 (ViewModel에 유지)
    private fun determineCardState(connState: InsoleConnectionState, isSaved: Boolean): InsoleCardState {
        return when (connState) {
            InsoleConnectionState.FULLY_CONNECTED -> InsoleCardState.CONNECTED
            InsoleConnectionState.PARTIALLY_CONNECTED,
            InsoleConnectionState.CONNECTING -> if (isSaved) InsoleCardState.DISCONNECTED_SAVED else InsoleCardState.DISCONNECTED_NO_SAVED // 연결 중에도 저장 여부 따라
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

    // GaitAnalysisResult 초기값 생성을 위한 헬퍼 함수 (필요시)
    private fun 초기값_GaitAnalysisResult_필요시_설정(): GaitAnalysisResult {
        // GaitAnalyzerUtil의 createEmptyResult()와 동일하게 생성하거나,
        // InsoleManager에서 초기값을 가진 Flow를 제공하도록 설계할 수 있음.
        // 현재 Manager의 realTimeAnalysisResult는 GaitAnalyzerUtil의 것을 쓰므로 이미 초기값을 가짐.
        // 따라서 별도 초기값 설정이 필수는 아님. stateIn의 기본값은 Flow의 첫번째 방출값 또는 지정값.
        // 여기서는 InsoleManager가 GaitAnalyzerUtil을 통해 초기값을 제공하므로,
        // ViewModel에서 추가적인 빈 객체 생성이 필요 없을 가능성이 높음.
        // 만약 stateIn에서 non-null 타입을 반환하고 싶고, Flow가 즉시 값을 방출하지 않는다면 초기값 명시 필요.
        // 하지만 GaitAnalyzerUtil.currentAnalysisResult는 MutableStateFlow이므로 항상 초기값을 가짐.
        return GaitAnalysisResult(timestamp = 0L) // 임시. 실제로는 Manager가 제공하는 초기값 사용.
    }


    override fun onCleared() {
        super.onCleared()
        // InsoleManager는 @Singleton이므로 ViewModel이 clear될 때 Manager를 clear하지 않음.
        // Manager의 생명주기는 Application 생명주기 또는 명시적인 clear() 호출에 따름.
        // 만약 ViewModel 세션에 특화된 작업을 Manager에서 중단해야 한다면,
        // insoleManager.onViewModelSessionEnded(viewModelId) 같은 메서드를 호출할 수 있지만,
        // 현재 요구사항에서는 불필요.
    }
}

