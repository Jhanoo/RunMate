package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.datasource.socket.SocketService
import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.MemberLeavedData
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import com.D107.runmate.domain.repository.socket.SocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val socketService: SocketService
) : SocketRepository {
    private val _memberLocationDatas = MutableStateFlow<Map<String, MemberLocationData>>(emptyMap())
    override val memberLocationDatas: StateFlow<Map<String, MemberLocationData>> = _memberLocationDatas.asStateFlow()

    override fun connect(auth: SocketAuth): Flow<ConnectionStatus> =
        socketService.connect(auth)

    override fun disconnect() {
        socketService.disconnect()
        clearMemberLocations()
    }

    override fun joinGroup(groupId: String) {
        socketService.joinGroup(groupId)
    }

    override fun sendLocationUpdate(lat: Double, lng: Double) {
        socketService.sendLocationUpdate(lat, lng)
    }

    override fun leaveGroup() {
        socketService.leaveGroup()
        _memberLocationDatas.value = emptyMap()
    }

    override fun observeLocationUpdates(): Flow<MemberLocationData> = flow {
        socketService.observeLocationUpdates().collect{ memberLocationData ->
            Timber.d("observeMemberLocationData $memberLocationData")
            _memberLocationDatas.update { currentMap ->
                val newMap = currentMap.toMutableMap()
                newMap[memberLocationData.userId] = memberLocationData
                newMap.toMap()
            }
            emit(memberLocationData)
        }
    }

    fun clearMemberLocations() {
        _memberLocationDatas.value = emptyMap()
        Timber.d("[SocketRepository] Cleared all member locations.")
    }

    override fun observeMemberLeaved(): Flow<MemberLeavedData> = flow{
        socketService.observeMemberLeaved().collect{
            emit(it)
        }
    }
    override fun connectionState(): StateFlow<Boolean> = socketService.isConnected()


}