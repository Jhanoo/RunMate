package com.D107.runmate.watch.presentation.menu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.watch.presentation.service.BluetoothService
import com.D107.runmate.watch.presentation.service.WearableService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val bluetoothService: BluetoothService,
    private val wearableService: WearableService
) : ViewModel() {

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()

    // 심박수 값 가져오기
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    // 토큰 상태
    private val _jwtToken = MutableStateFlow<String?>(null)
    val jwtToken: StateFlow<String?> = _jwtToken.asStateFlow()

    init {
        // 토큰 상태 수집
        viewModelScope.launch {
            wearableService.jwtToken.collect { token ->
                _jwtToken.value = token
                Log.d("Token", "JWT 토큰 상태 변경: ${token?.take(10)}...")
            }
        }

        viewModelScope.launch {
            wearableService.connectedToPhone.collect { isConnected ->
                _isBluetoothConnected.value = isConnected
                Log.d("WearableStatus", "폰 연결 상태: ${if (isConnected) "연결됨" else "연결 안됨"}")
            }
//            bluetoothService.connectionState.collect { isConnected ->
//                _isBluetoothConnected.value = isConnected
//                // 블루투스 연결 상태 로그
//                Log.d("BluetoothStatus", "블루투스 연결 상태: ${if(isConnected) "연결됨" else "연결 안됨"}")
//            }
        }

        connectToPhone()

        // 지속적으로 상태 로깅
        startStatusLogging()

        // 저장된 토큰 확인
        checkSavedToken()
    }

    private fun checkSavedToken() {
        viewModelScope.launch {
            val token = wearableService.getJwtToken()
            Log.d("Token", "저장된 JWT 토큰: ${token?.take(10) ?: "없음"}...")
        }
    }

    // 심박수 값 설정 메서드
    fun updateHeartRate(hr: Int) {
        _heartRate.value = hr
    }

    fun connectToPhone() {
        viewModelScope.launch {
            try {
                Log.d("Wearable", "폰과 연결 시도 중...")
                // Wearable 연결 사용
                wearableService.checkConnection()

                // 잠시 대기 후 상태 확인
                delay(2000)
                val connected = wearableService.connectedToPhone.value
                Log.d("Wearable", "폰 연결 상태: ${if(connected) "연결됨" else "연결 실패"}")

                // 상태 업데이트
                _isBluetoothConnected.value = connected

                // 활성 데이터 항목 확인 (이 부분 추가)
                if (connected) {
                    wearableService.checkActiveDataItems()
                }
            } catch (e: Exception) {
                Log.e("Wearable", "연결 확인 실패: ${e.message}")
            }
        }
    }

    // 주기적으로 상태 로깅
    private fun startStatusLogging() {
        viewModelScope.launch {
            while (true) {
                val connected = wearableService.connectedToPhone.value
                val heartRate = _heartRate.value

                // 상태 로그 출력
                Log.d(
                    "WatchStatus", "폰 연결: ${if (connected) "연결됨" else "연결 안됨"}, " +
                            "HR: $heartRate" +
                            "${if (connected) " (전송 중)" else ""}"
                )

                delay(5000) // 5초마다 로그 출력
            }
        }
    }
}