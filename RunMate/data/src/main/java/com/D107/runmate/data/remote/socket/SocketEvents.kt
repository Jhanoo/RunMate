package com.D107.runmate.data.remote.socket

object SocketEvents {
    const val JOIN_GROUP = "joinGroup"
    const val LOCATION_UPDATE_OUTGOING = "locationUpdate" // 서버로 보내는 이벤트
    const val LEAVE_GROUP = "leaveGroup"

    const val LOCATION_UPDATE_INCOMING = "locationUpdate" // 서버에서 받는 이벤트

    const val MEMBER_LEAVED = "memberLeaved"

    const val CONNECT = "connect"
    const val DISCONNECT = "disconnect"
    const val CONNECT_ERROR = "connect_error"
}