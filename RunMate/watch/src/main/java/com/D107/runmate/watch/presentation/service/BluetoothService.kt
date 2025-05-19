package com.D107.runmate.watch.presentation.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "BluetoothService"
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private val SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // 기본 SPP UUID

    private var connectedDeviceAddress: String? = null

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    // 블루투스 연결 상태 확인
    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }


    @SuppressLint("MissingPermission")
    // 블루투스 연결 시도
    suspend fun connectToDevice(deviceAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 기존에 연결된 소켓이 있다면 닫기
                if (bluetoothSocket?.isConnected == true) {
                    bluetoothSocket?.close()
                }

                // 주소로 원격 블루투스 기기 가져오기 및 소켓 생성
                val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                bluetoothSocket = device?.createRfcommSocketToServiceRecord(SERVICE_UUID)
                bluetoothSocket?.connect()

                // 연결된 기기 주소 저장
                connectedDeviceAddress = deviceAddress
                Log.d(TAG, "Successfully connected to device: $deviceAddress")

                _connectionState.value = true

                true
            } catch (e: IOException) {
                // 연결 실패 시 로그 출력 및 소켓 정리
                Log.e(TAG, "Failed to connect: ${e.message}")
                bluetoothSocket?.close()
                bluetoothSocket = null
                false
            }
        }
    }

    // 앱으로 심박수 데이터 전송
    suspend fun sendHeartRate(heartRate: Int): Boolean {
        Log.d(TAG, "Heart rate measured: $heartRate")

        if (bluetoothSocket?.isConnected != true) {
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                // 심박수 데이터를 문자열 형태로 전송
                val message = "HR:$heartRate"
                bluetoothSocket?.outputStream?.write(message.toByteArray())
                Log.d(TAG, "Heart rate sent: $heartRate")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send heart rate: ${e.message}")
                false
            }
        }

    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startConnectionToPhone() {
        // 기기 검색 및 연결 시도
        serviceScope.launch {
            try {
                // 페어링된 기기 목록 가져오기
                @SuppressLint("MissingPermission")
                val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()

                if (pairedDevices.isEmpty()) {
                    Log.d(TAG, "페어링된 기기가 없습니다")
                    return@launch
                }

                // 페어링된 기기 중 모바일 기기 찾기
                for (device in pairedDevices) {
                    // 모바일 기기인지 확인 (클래스가 PHONE인 경우)
                    @SuppressLint("MissingPermission")
                    if (device.bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.PHONE) {
                        Log.d(TAG, "폰 기기 발견: ${device.name}, ${device.address}")

                        // 연결 시도
                        val connected = connectToDevice(device.address)
                        if (connected) {
                            Log.d(TAG, "폰과 블루투스 연결 성공: ${device.name}")
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "블루투스 연결 시도 중 오류: ${e.message}")
            }
        }
    }

    // 리소스 해제를 위한 메서드 추가
    fun cleanup() {
        serviceScope.cancel() // 코루틴 스코프 취소
        disconnect() // 블루투스 연결 해제
    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            connectedDeviceAddress = null
            _connectionState.value = false
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket: ${e.message}")
        }
    }

    // 연결 상태 확인
    fun observeConnectionState(): StateFlow<Boolean> {
        return connectionState
    }

    // 러닝 데이터 전송 (심박수)
    suspend fun sendRunningData(heartRate: Int, pace: String, distance: Double, cadence: Int): Boolean {
        Log.d(TAG, "Heart rate measured: $heartRate, 연결 상태: ${isConnected()}")

        if (bluetoothSocket?.isConnected != true) {
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                // 데이터를 JSON 형태로 구성
                val runningData = """{"hr":$heartRate,"pace":"$pace","distance":$distance,"cadence":$cadence}"""
                val message = "DATA:$runningData"
                bluetoothSocket?.outputStream?.write(message.toByteArray())
                Log.d(TAG, "Running data sent: $message")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Failed to send running data: ${e.message}")
                false
            }
        }
    }
}