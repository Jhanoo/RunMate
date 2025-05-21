package com.D107.runmate.data.remote.datasource.socket

import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.data.remote.socket.SocketEvents
import com.D107.runmate.domain.model.socket.MemberLeavedData
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import timber.log.Timber
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketService @Inject constructor() {

    private var socket: Socket? = null
    private val SERVER_URL = "http://k12d107.p.ssafy.io:3000"

    private val _isConnectedStateFlow = MutableStateFlow<Boolean>(false)
    val isConnectedStateFlow: StateFlow<Boolean> = _isConnectedStateFlow.asStateFlow()


    fun connect(auth: SocketAuth): Flow<ConnectionStatus> = callbackFlow {
        if (socket?.isActive == true) {
            trySend(ConnectionStatus.Connected)
            awaitClose { } // 이미 연결된 경우 즉시 닫음
            return@callbackFlow
        }

        _isConnectedStateFlow.value = false

        val authMap = mapOf(
            "userId" to auth.userId,
            "nickname" to auth.nickname,
            "profileImage" to auth.profileImage
        )

        val opts = IO.Options().apply {
            this.auth = authMap
            transports = arrayOf("websocket")
        }

        try {
            Timber.d("New Socket (Received)")
            socket = IO.socket(SERVER_URL, opts)
        } catch (e: URISyntaxException) {
            Timber.e(e, "Socket URL Syntax Exception")
            trySend(ConnectionStatus.Error("URL Syntax Error: ${e.message}"))
            close(e)
            return@callbackFlow
        }

        socket?.on(SocketEvents.CONNECT) {
            Timber.i("Socket connected: ${socket?.id()}")
            _isConnectedStateFlow.value = true
            trySend(ConnectionStatus.Connected)
        }

        socket?.on(SocketEvents.DISCONNECT) { args ->
            val reason = args.getOrNull(0)?.toString() ?: "Unknown reason"
            Timber.i("Socket disconnected: $reason")
            _isConnectedStateFlow.value = false
            trySend(ConnectionStatus.Disconnected(reason))
        }

        socket?.on(SocketEvents.CONNECT_ERROR) { args ->
            val error = args.getOrNull(0)
            Timber.e("Socket connection error: $error")
            val errorMessage = when (error) {
                is String -> error
                is Exception -> error.message ?: "Unknown connection error"
                else -> error?.toString() ?: "Unknown connection error"
            }
            _isConnectedStateFlow.value = false
            trySend(ConnectionStatus.Error(errorMessage))
        }

        socket?.on(SocketEvents.MEMBER_LEAVED) {
            Timber.d("Received ${SocketEvents.MEMBER_LEAVED}: ${it.firstOrNull()}")
        }

        socket?.connect()

        awaitClose {
            Timber.i("Socket connection flow closing. Disconnecting socket.")
            socket?.off(SocketEvents.CONNECT)
            socket?.off(SocketEvents.DISCONNECT)
            socket?.off(SocketEvents.CONNECT_ERROR)
            // 연결 해제 시 등록한 다른 이벤트 리스너들도 off 처리 필요
            socket?.disconnect()
            socket = null
        }
    }

    fun disconnect() {
        Timber.i("Socket disconnect requested.")
        socket?.disconnect()
        _isConnectedStateFlow.value = false
    }

    fun joinGroup(groupId: String) {
        if (socket?.connected() == true) {
            val data = JSONObject().apply { put("groupId", groupId) }
            socket?.emit(SocketEvents.JOIN_GROUP, data)
            Timber.d("Emitted ${SocketEvents.JOIN_GROUP}: $data")
        } else {
            Timber.w("Cannot emit ${SocketEvents.JOIN_GROUP}, socket not connected.")
        }
    }

    fun sendLocationUpdate(lat: Double, lng: Double) {
        if (socket?.connected() == true) {
            val data = JSONObject().apply {
                put("lat", lat)
                put("lng", lng)
            }
            socket?.emit(SocketEvents.LOCATION_UPDATE_OUTGOING, data)
            Timber.d("Emitted ${SocketEvents.LOCATION_UPDATE_OUTGOING}: $data")
        } else {
            Timber.w("Cannot emit ${SocketEvents.LOCATION_UPDATE_OUTGOING}, socket not connected.")
        }
    }

    fun leaveGroup() {
        if (socket?.connected() == true) {
            socket?.emit(SocketEvents.LEAVE_GROUP)
            Timber.d("Emitted ${SocketEvents.LEAVE_GROUP}")
        } else {
            Timber.w("Cannot emit ${SocketEvents.LEAVE_GROUP}, socket not connected.")
        }
    }


    fun observeLocationUpdates(): Flow<MemberLocationData> = callbackFlow {
        if (socket == null) {
            Timber.w("Socket is null, cannot listen to ${SocketEvents.LOCATION_UPDATE_INCOMING}")
            close(IllegalStateException("Socket not initialized before observing location updates."))
            return@callbackFlow
        }

        val listener = Emitter.Listener { args ->
            Timber.d("Received ${SocketEvents.LOCATION_UPDATE_INCOMING}: ${args.firstOrNull()}")
            val data = args.firstOrNull() as? JSONObject
            if (data != null) {
                try {
                    val memberLocationData = MemberLocationData(
                        userId = data.getString("userId"),
                        nickname = data.getString("nickname"),
                        profileImage = if(data.optString("profileImage", null)=="null") null else data.optString("profileImage", null),
                        lat = data.getDouble("lat"),
                        lng = data.getDouble("lng"),
                        timestamp = data.getLong("timestamp")
                    )
                    trySend(memberLocationData)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing location update data")
                }
            } else {
                Timber.w("${SocketEvents.LOCATION_UPDATE_INCOMING} data is null or not JSONObject. Data: ${args.firstOrNull()}")
            }
        }

        socket?.on(SocketEvents.LOCATION_UPDATE_INCOMING, listener)
        Timber.i("Listener for ${SocketEvents.LOCATION_UPDATE_INCOMING} registered.")

        awaitClose {
            Timber.i("Location updates flow closing. Removing listener for ${SocketEvents.LOCATION_UPDATE_INCOMING}.")
            socket?.off(SocketEvents.LOCATION_UPDATE_INCOMING, listener)
        }
    }


    fun observeMemberLeaved(): Flow<MemberLeavedData> = callbackFlow {
        if (socket == null) {
            Timber.w("Socket is null, cannot listen to ${SocketEvents.MEMBER_LEAVED}")
            close(IllegalStateException("Socket not initialized before observing member leaved events."))
            return@callbackFlow
        }

        val listener = Emitter.Listener { args ->
            Timber.d("Received ${SocketEvents.MEMBER_LEAVED}: ${args.firstOrNull()}")
            val data = args.firstOrNull() as? JSONObject
            if (data != null) {
                try {
                    val memberLeavedData = MemberLeavedData(
                        userId = data.getString("userId")
                    )
                    trySend(memberLeavedData)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing member leaved data")
                }
            } else {
                Timber.w("${SocketEvents.MEMBER_LEAVED} event data is null or not a JSONObject. Data: ${args.firstOrNull()}")
            }
        }

        socket?.on(SocketEvents.MEMBER_LEAVED, listener)
        Timber.i("Listener for ${SocketEvents.MEMBER_LEAVED} registered.")

        awaitClose {
            Timber.i("Member leaved flow closing. Removing listener for ${SocketEvents.MEMBER_LEAVED}.")
            socket?.off(SocketEvents.MEMBER_LEAVED, listener)
        }
    }


    fun isConnected(): StateFlow<Boolean> = isConnectedStateFlow

}

