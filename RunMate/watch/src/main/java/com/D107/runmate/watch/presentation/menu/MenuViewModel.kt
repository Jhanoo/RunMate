package com.D107.runmate.watch.presentation.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.watch.presentation.service.BluetoothService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val bluetoothService: BluetoothService
) : ViewModel() {

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()

    // 심박수 값 가져오기
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    init {
        viewModelScope.launch {
            bluetoothService.connectionState.collect { isConnected ->
                _isBluetoothConnected.value = isConnected
                // 블루투스 연결 상태 로그
                Log.d("BluetoothStatus", "블루투스 연결 상태: ${if(isConnected) "연결됨" else "연결 안됨"}")
            }
        }

        // 지속적으로 상태 로깅
        startStatusLogging()
    }

    // 심박수 값 설정 메서드
    fun updateHeartRate(hr: Int) {
        _heartRate.value = hr
    }

    // 주기적으로 상태 로깅
    private fun startStatusLogging() {
        viewModelScope.launch {
            while(true) {
                val connected = bluetoothService.isConnected()
                val heartRate = _heartRate.value

                // 상태 로그 출력
                Log.d("WatchStatus", "블루투스: ${if(connected) "연결됨" else "연결 안됨"}, " +
                        "HR: $heartRate" +
                        "${if(connected) " (전송 중)" else ""}")

                delay(5000) // 5초마다 로그 출력
            }
        }
    }
}