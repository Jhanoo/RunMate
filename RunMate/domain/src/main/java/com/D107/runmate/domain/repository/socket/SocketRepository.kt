package com.D107.runmate.domain.repository.socket

import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import kotlinx.coroutines.flow.Flow

interface SocketRepository {
    fun connect(auth: SocketAuth): Flow<ConnectionStatus>
    fun disconnect()
    fun joinGroup(groupId: String)
    fun sendLocationUpdate(lat: Double, lng: Double)
    fun leaveGroup()
    fun observeLocationUpdates(): Flow<MemberLocationData>
    fun isConnected(): Boolean
}