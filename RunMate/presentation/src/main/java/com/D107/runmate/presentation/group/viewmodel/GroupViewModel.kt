package com.D107.runmate.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.GroupData
import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import com.D107.runmate.domain.usecase.group.FinishGroupUseCase
import com.D107.runmate.domain.usecase.group.GetCurrentGroupUseCase
import com.D107.runmate.domain.usecase.group.JoinGroupUseCase
import com.D107.runmate.domain.usecase.group.LeaveGroupUseCase
import com.D107.runmate.domain.usecase.group.StartGroupUseCase
import com.D107.runmate.domain.usecase.socket.ConnectSocketUseCase
import com.D107.runmate.domain.usecase.socket.DisconnectSocketUseCase
import com.D107.runmate.domain.usecase.socket.HasGroupHistoryUseCase
import com.D107.runmate.domain.usecase.socket.IsSocketConnectedUseCase
import com.D107.runmate.domain.usecase.socket.JoinGroupSocketUseCase
import com.D107.runmate.domain.usecase.socket.LeaveGroupSocketUseCase
import com.D107.runmate.domain.usecase.socket.ObserveLocationUpdatesUseCase
import com.D107.runmate.domain.usecase.socket.ObserveMemberLeavedUseCase
import com.D107.runmate.domain.usecase.socket.ObserveMemberLocationDatasUsecase
import com.D107.runmate.domain.usecase.socket.SendLocationUpdateUseCase
import com.kakao.vectormap.label.Label
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getCurrenGroupUseCase: GetCurrentGroupUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val joinGroupUseCase: JoinGroupUseCase,
    private val startGroupUseCase: StartGroupUseCase,
    private val connectSocketUseCase: ConnectSocketUseCase,
    private val disconnectSocketUseCase: DisconnectSocketUseCase,
    private val isSocketConnectedUseCase: IsSocketConnectedUseCase,
    private val joinGroupSocketUseCase: JoinGroupSocketUseCase,
    private val sendLocationUseCase: SendLocationUpdateUseCase,
    private val leaveGroupSocketUseCase: LeaveGroupSocketUseCase,
    private val observeLocationUpdatesUseCase: ObserveLocationUpdatesUseCase,
    private val observeMemberLeavedUseCase: ObserveMemberLeavedUseCase,
    private val observeMemberLocationDatasUseCase: ObserveMemberLocationDatasUsecase,
    private val finishGroupUseCase: FinishGroupUseCase,
    private val hasGroupHistoryUseCase : HasGroupHistoryUseCase
) : ViewModel() {



    private val _currentGroup = MutableStateFlow<GroupData?>(null)
    val currentGroup = _currentGroup.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Initial)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _groupMemberLocation = MutableStateFlow<MemberLocationData?>(null) // 마지막 위치만 표시하거나 List로 관리
    val groupMemberLocation: StateFlow<MemberLocationData?> = _groupMemberLocation.asStateFlow()

    var groupMemberDatas : StateFlow<Map<String, MemberLocationData>> =
    observeMemberLocationDatasUseCase()
    .stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyMap()
    )

    var isUserLabel = true

    private var locationObserverJob: Job? = null

    private var memberLeaveObserverJob: Job? = null

    private var connectionJob: Job? = null

    private val _uiEvent = MutableSharedFlow<GroupUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()



    fun getCurrentGroup() {
        Timber.d("GetCurrentGroup")
        viewModelScope.launch {

            getCurrenGroupUseCase().collect { result ->
//                _isLoading.value = false
                if (result is ResponseStatus.Success) {
                    Timber.d("GetCurrentGroup Success")
                    if (result.data != null) {
                        _currentGroup.value = result.data
                        Timber.d("${result.data}")
                        result.data?.status.let{status ->
                            Timber.d("group status $status")
                            when(status) {
                                0 -> {
                                    _uiEvent.emit(GroupUiEvent.GoToGroupInfo)
                                }
                                1 -> {
                                    _uiEvent.emit(GroupUiEvent.GoToGroupRunning)
                                }
                                else -> {
                                    _uiEvent.emit(GroupUiEvent.ToggleGroupFragmentVisible(false))

                                }
                            }

                        }

                    } else {
                        _uiEvent.emit(GroupUiEvent.ToggleGroupFragmentVisible(true))
                    }

                } else if (result is ResponseStatus.Error) {
                    Timber.d("GetCurrentGroup Fail")
                    _currentGroup.value = null
                    _uiEvent.emit(GroupUiEvent.ToggleGroupFragmentVisible(true))

                }

            }


        }
    }

    fun leaveGroup() {
        viewModelScope.launch {
            leaveGroupUseCase().collect { result ->
                if (result is ResponseStatus.Success) {
                    Timber.d("LeaveGroup Success")
                    _currentGroup.value = null
                    _uiEvent.emit(GroupUiEvent.GoToGroup)
                }
            }
        }
    }

    fun submitInvitationCode(inviteCode: String) {
        viewModelScope.launch {
            joinGroupUseCase(inviteCode).collect { result ->
                if (result is ResponseStatus.Success) {
                    Timber.d("JoinGroup Success")
                    if (result.data != null) {
                        getCurrentGroup()
                    } else {
                        _uiEvent.emit(GroupUiEvent.ShowToast("그룹에 가입할 수 없습니다."))
                    }
                } else {
                    _uiEvent.emit(GroupUiEvent.ShowToast("그룹에 가입할 수 없습니다."))
                }
            }
        }
    }

    fun startGroup(){
        viewModelScope.launch {
            startGroupUseCase().collect{result->
                if(result is ResponseStatus.Success){
                    Timber.d("StartGroup Success")
                    _uiEvent.emit(GroupUiEvent.GoToGroupRunning)
                }
            }
        }
    }

    fun finishGroup(){
        viewModelScope.launch {
            finishGroupUseCase().collect { result ->
                if (result is ResponseStatus.Success) {
                    Timber.d("FinishGroup Success")
                    _uiEvent.emit(GroupUiEvent.GoToGroup)
                }
            }
        }
    }

    fun hasGroupHistory(){
        viewModelScope.launch {
            hasGroupHistoryUseCase().collect{result->
                if(result is ResponseStatus.Success){
                    _uiEvent.emit(GroupUiEvent.ToggleGroupRunningFinishVisible(result.data))
                }
            }
        }
    }

    fun connectToServer(socketAuth: SocketAuth) {
        if (isSocketConnectedUseCase().value) {
            _connectionStatus.value = ConnectionStatus.AlreadyConnected
            Timber.i("Already connected to socket.")
            joinGroupSocket() // 이미 연결되어 있으면 관찰 시작
            return
        }

        viewModelScope.launch {
            connectSocketUseCase(socketAuth)
                .catch { e ->
                    Timber.e(e, "Connection Flow Error")
                    _connectionStatus.value = ConnectionStatus.Error("Flow collection error: ${e.message}")
                }
                .collect { status ->
                    _connectionStatus.value = status
                    Timber.d("SocketStatus : ${_connectionStatus.value}")
                    if (status is ConnectionStatus.Connected) {
                        Timber.d("SocketConnected")
                        joinGroupSocket()
                    } else if (status is ConnectionStatus.Disconnected || status is ConnectionStatus.Error) {
                        Timber.d("SocketStop")
                        stopObservingLocationUpdates()
                    }
                }
        }
    }

    fun joinGroupSocket() {
        viewModelScope.launch {
            if ((currentGroup.value?.status ?: 0) == 1) {
                joinGroupSocketUseCase(currentGroup.value?.groupId?:"")
                startObservingLocationUpdates()
                startObservingMemberLeave()
            } else {
                Timber.w("Socket not connected. Cannot join group.")
            }
        }
    }

    fun sendLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            if (isSocketConnectedUseCase().value) {
                sendLocationUseCase(lat, lng)
            } else {
                Timber.w("Socket not connected. Cannot send location.")
            }
        }
    }

    fun leaveGroupSocket() {
        viewModelScope.launch {
            if (isSocketConnectedUseCase().value) {
                leaveGroupSocketUseCase()
            } else {
                Timber.e("Socket not connected. Cannot leave group.")
            }
        }
    }

    fun startObservingLocationUpdates() {
        Timber.d("SocketObserveStart0")
        if (locationObserverJob?.isActive == true) return
        Timber.d("SocketObserveStart")
        locationObserverJob = viewModelScope.launch {
            observeLocationUpdatesUseCase()
                .catch { e -> Timber.e(e, "Error observing location updates") }
                .collect { locationData ->
                    _groupMemberLocation.value = locationData
                    Timber.d("New location update received in ViewModel: $locationData")
                }
        }
    }

    fun startObservingMemberLeave(){
        if (locationObserverJob?.isActive == true) return
        memberLeaveObserverJob = viewModelScope.launch {
            observeMemberLeavedUseCase().collect{ memberLeavedData ->
                if(memberLeavedData.userId == currentGroup.value?.leaderId){
//                    _uiEvent.emit(GroupUiEvent.ShowFinishDialog)
                }

            }
        }

    }

    fun stopObservingLocationUpdates() {
        locationObserverJob?.cancel()
        locationObserverJob = null
    }

    fun disConnectSocket(){
        disconnectSocketUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("ViewModel cleared. Disconnecting socket.")
        stopObservingLocationUpdates()
    }




}