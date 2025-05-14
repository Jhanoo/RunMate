package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.datasource.socket.SocketService
import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import com.D107.runmate.domain.repository.socket.SocketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val socketService: SocketService
) : SocketRepository {

    override fun connect(auth: SocketAuth): Flow<ConnectionStatus> =
        socketService.connect(auth)

    override fun disconnect() {
        socketService.disconnect()
    }

    override fun joinGroup(groupId: String) {
        socketService.joinGroup(groupId)
    }

    override fun sendLocationUpdate(lat: Double, lng: Double) {
        socketService.sendLocationUpdate(lat, lng)
    }

    override fun leaveGroup() {
        socketService.leaveGroup()
    }

    override fun observeLocationUpdates(): Flow<MemberLocationData> =
        socketService.observeLocationUpdates()

    override fun isConnected(): Boolean = socketService.isConnected()
}