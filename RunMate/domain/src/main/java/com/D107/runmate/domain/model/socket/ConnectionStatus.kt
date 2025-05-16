package com.D107.runmate.domain.model.socket

sealed class ConnectionStatus {
    data object Connected : ConnectionStatus()
    data object AlreadyConnected : ConnectionStatus() // 이미 연결되어 있을 때
    data class Disconnected(val reason: String) : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
    data object Initial: ConnectionStatus() // 초기 상태
}