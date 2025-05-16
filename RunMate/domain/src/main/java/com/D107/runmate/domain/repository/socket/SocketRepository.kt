package com.D107.runmate.domain.repository.socket

import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.MemberLeavedData
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SocketRepository {
    val memberLocationDatas: StateFlow<Map<String, MemberLocationData>>
    fun connect(auth: SocketAuth): Flow<ConnectionStatus>
    fun disconnect()
    fun joinGroup(groupId: String)
    fun sendLocationUpdate(lat: Double, lng: Double)
    fun leaveGroup()
    fun observeLocationUpdates(): Flow<MemberLocationData>
    fun observeMemberLeaved(): Flow<MemberLeavedData>
    fun connectionState(): StateFlow<Boolean>
}