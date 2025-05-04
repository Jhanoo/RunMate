package com.D107.runmate.data.remote.datasource

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.model.Insole.InsoleSide
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class BleDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {



    private val bluetoothLeScanner by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    // --- 페어 관리 변수 ---
    private var leftGatt: BluetoothGatt? = null
    private var rightGatt: BluetoothGatt? = null



    private val _leftConnectionState = MutableStateFlow(DeviceConnectionState.DISCONNECTED)
    private val _rightConnectionState = MutableStateFlow(DeviceConnectionState.DISCONNECTED)

    private val _pairConnectionState = MutableStateFlow(InsoleConnectionState.DISCONNECTED)
    val pairConnectionState: StateFlow<InsoleConnectionState> = _pairConnectionState.asStateFlow()

    private val _leftRawData = MutableSharedFlow<ByteArray?>(replay = 1)
    private val _rightRawData = MutableSharedFlow<ByteArray?>(replay = 1)

    //왼발, 오른발 통합 정보
    val combinedSensorDataFlow: Flow<Pair<ByteArray?, ByteArray?>> =
        _leftRawData.combine(_rightRawData) { leftData, rightData ->
            Pair(leftData, rightData)
        }
            .onStart { emit(Pair(_leftRawData.replayCache.firstOrNull(), _rightRawData.replayCache.firstOrNull())) }
            .shareIn(CoroutineScope(ioDispatcher), SharingStarted.WhileSubscribed())


    // 기기 스캔
    fun scanDevices(): Flow<List<ScanResult>> {
        Timber.d("scanDevices function called.")

        val scanner = bluetoothLeScanner
        if (scanner == null) {
            Timber.e("BluetoothLeScanner is null, cannot start scan. Returning empty flow.")
            return emptyFlow()
        }

        return callbackFlow {

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                Timber.w("BLE 스캔 시작 불가 (callbackFlow 내부 체크): 어댑터 null 또는 비활성화")
                close(IllegalStateException("Bluetooth not available or disabled"))
                return@callbackFlow
            }

            val scanResults = mutableListOf<ScanResult>()

            val scanFilters = listOf(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
                    .build()
            )
            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result?.let {
                        val existingIndex = scanResults.indexOfFirst {
                            it.device.address == result.device.address
                        }

                        if (existingIndex >= 0) {
                            scanResults[existingIndex] = result
                        } else {
                            scanResults.add(result)
                        }

                        trySend(scanResults.toList())
                    }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    Timber.d("onBatchScanResults: ${results?.size ?: 0} results")
                    results?.forEach { result ->
                        val existingIndex = scanResults.indexOfFirst {
                            it.device.address == result.device.address
                        }

                        if (existingIndex >= 0) {
                            scanResults[existingIndex] = result
                        } else {
                            scanResults.add(result)
                        }
                    }
                    trySend(scanResults.toList())
                }

                override fun onScanFailed(errorCode: Int) {
                    Timber.e("!!! BLE 스캔 실패 (onScanFailed callback): $errorCode")
                    close(RuntimeException("BLE Scan failed with error code $errorCode"))
                }
            }

            try {
                Timber.d("Attempting to start scan...")
                scanner.startScan(scanFilters, scanSettings, scanCallback)
                Timber.i(">>> BLE scan started successfully <<<")
            } catch (e: SecurityException) {
                Timber.e(e, "!!! SecurityException during startScan !!! Check Permissions!")
                close(e)
                return@callbackFlow
            } catch (e: Exception) {
                Timber.e(e, "!!! Exception during startScan !!!")
                close(e)
                return@callbackFlow
            }

            awaitClose {
                Timber.d("<<< awaitClose executing: Stopping scan... >>>")
                try {
                    Timber.d("Calling stopScan...")
                    scanner.stopScan(scanCallback)
                    Timber.i("stopScan called successfully.")
                } catch (e: SecurityException) {
                    Timber.e(e, "!!! SecurityException during stopScan !!!")
                } catch (e: Exception) {
                    Timber.e(e, "스캔 중지 중 오류 발생: ${e.message}")
                }
            }
        }.flowOn(ioDispatcher)
    }
    

    // 양발 공통으로 사용
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address ?: "Unknown"
            val side = getSideFromGatt(gatt) ?: return // 어느 쪽 GATT인지 식별

            Timber.d("[$side] onConnectionStateChange: Address=$deviceAddress, Status=$status, NewState=$newState")

            val currentDeviceState = when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Timber.i("[$side] 성공적으로 연결됨: $deviceAddress")
                        CoroutineScope(ioDispatcher).launch {
                            delay(600) // 서비스 탐색 전 안정화 시간 (조절 가능)
                            val discovered = gatt?.discoverServices()
                            if (discovered == true) {
                                Timber.d("[$side] 서비스 탐색 시작됨.")
                            } else {
                                Timber.e("[$side] 서비스 탐색 시작 실패.")
                                handleConnectionFailure(gatt, side, "Service discovery start failed")
                            }
                        }
                        DeviceConnectionState.CONNECTING
                    } else {
                        Timber.e("[$side] 연결 실패 (상태 코드: $status): $deviceAddress")
                        handleConnectionFailure(gatt, side, "Connection failed with status $status")
                        DeviceConnectionState.FAILED
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Timber.i("[$side] 연결 끊김: $deviceAddress (Status: $status)")
                    gatt?.close()
                    if (side == InsoleSide.LEFT) leftGatt = null else rightGatt = null
                    DeviceConnectionState.DISCONNECTED
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    Timber.d("[$side] 연결 중...: $deviceAddress")
                    DeviceConnectionState.CONNECTING
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Timber.d("[$side] 연결 해제 중...: $deviceAddress")
                    getCurrentDeviceState(side) // 이전 상태 유지
                }
                else -> getCurrentDeviceState(side) // 알 수 없는 상태는 이전 상태 유지
            }

            // 해당 Side의 내부 상태 업데이트
            updateDeviceState(side, currentDeviceState)

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            val side = getSideFromGatt(gatt) ?: return

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.d("[$side] 서비스 탐색 완료.")
                val requested = gatt?.requestMtu(BleConstants.REQUEST_MTU_SIZE)
                // Notification 활성화 시도
                if (requested == true) {
                    Timber.d("[$side] MTU request initiated.")
                    enableSensorNotifications(gatt, side)
                } else {
                    Timber.e("[$side] Failed to initiate MTU request.")
                    enableSensorNotifications(gatt, side)
                }
            } else {
                Timber.w("[$side] 서비스 탐색 실패: $status")
                handleConnectionFailure(gatt, side, "Service discovery failed with status $status")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            val side = getSideFromGatt(gatt) ?: return

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.i("[$side] MTU changed successfully to: $mtu bytes")
            } else {
                Timber.e("[$side] Failed to change MTU. Status: $status")
            }
        }

        // API 33이상
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
            handleCharacteristicRead(gatt, characteristic, value, status)
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
            handleCharacteristicChange(gatt, characteristic, value)
        }

        // API 32이하
        @Deprecated("Use overload with byte array value")
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return
            handleCharacteristicRead(gatt, characteristic, characteristic?.value, status) // value 직접 접근
        }
        @Deprecated("Use overload with byte array value")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return
            handleCharacteristicChange(gatt, characteristic, characteristic?.value) // value 직접 접근
        }

        // 공통 로직 처리 함수
        private fun handleCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, value: ByteArray?, status: Int) {
            val side = getSideFromGatt(gatt) ?: return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic?.uuid == BleConstants.SENSOR_CHARACTERISTIC_UUID && value != null) {
                    Timber.d("[$side] 센서 데이터 읽기 성공: ${value.size} bytes")
                }
            } else {
                Timber.w("[$side] 캐릭터리스틱 읽기 실패: $status")
            }
        }

        private fun handleCharacteristicChange(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, value: ByteArray?) {
            val side = getSideFromGatt(gatt) ?: return
            if (characteristic?.uuid == BleConstants.SENSOR_CHARACTERISTIC_UUID && value != null) {
                // 해당 Side의 데이터 Flow로 방출
                val flow = if (side == InsoleSide.LEFT) _leftRawData else _rightRawData
                val emitted = flow.tryEmit(value)
                if (!emitted) {
                    Timber.w("[$side] 센서 데이터 Flow 방출 실패 (버퍼 가득 참?)")
                }
            } else if (value == null) {
                Timber.w("[$side] 수신된 센서 데이터가 null입니다.")
            }
        }


        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            val side = getSideFromGatt(gatt) ?: return

            if (descriptor?.characteristic?.uuid == BleConstants.SENSOR_CHARACTERISTIC_UUID) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Timber.i("[$side] 센서 Notification 활성화 성공!")
                    // Notification 설정 완료 후 해당 장치 상태를 CONNECTED로 변경
                    updateDeviceState(side, DeviceConnectionState.CONNECTED)
                } else {
                    Timber.e("[$side] 센서 Notification 활성화 실패: $status")
                    handleConnectionFailure(gatt, side, "Descriptor write failed with status $status")
                }
            }
        }
    }

    //페어 연결
    fun connectPair(leftAddress: String, rightAddress: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Timber.w("페어 연결 불가: 블루투스 비활성화 상태")
            updatePairConnectionStateBasedOnIndividualStates() // 상태 업데이트 시도 (DISCONNECTED/FAILED)
            return
        }

        if (leftGatt != null || rightGatt != null) {
            Timber.w("기존 연결 감지. 페어 연결 해제 후 재시도.")
            disconnectPair()
            // disconnectPair가 비동기 콜백을 통해 상태를 변경하므로,
            // 약간의 딜레이 후 재연결 시도 또는 상태 관찰 후 재연결 필요
            CoroutineScope(Dispatchers.Main).launch { // UI 스레드에서 잠시 대기 후 재시도
                delay(1000)
                if (pairConnectionState.value == InsoleConnectionState.DISCONNECTED) {
                    connectPairInternal(leftAddress, rightAddress)
                } else {
                    Timber.w("페어 해제 후에도 상태가 DISCONNECTED가 아님. 연결 재시도 취소.")
                }
            }
        } else {
            connectPairInternal(leftAddress, rightAddress)
        }
    }

    private fun connectPairInternal(leftAddress: String, rightAddress: String) {
        Timber.d("페어 연결 시도: Left=$leftAddress, Right=$rightAddress")
        _pairConnectionState.value = InsoleConnectionState.CONNECTING // 통합 상태 변경

        // 각 장치 연결 시도
        connectDeviceInternal(leftAddress, InsoleSide.LEFT)
        connectDeviceInternal(rightAddress, InsoleSide.RIGHT)
    }


    private fun connectDeviceInternal(address: String, side: InsoleSide) {
        val device: BluetoothDevice? = try {
            bluetoothAdapter?.getRemoteDevice(address)
        } catch (e: IllegalArgumentException) {
            Timber.e("[$side] 잘못된 블루투스 주소: $address")
            null
        }

        if (device == null) {
            Timber.e("[$side] 기기($address)를 찾을 수 없음.")
            handleConnectionFailure(null, side, "Device not found")
            return
        }

        updateDeviceState(side, DeviceConnectionState.CONNECTING)
        Timber.d("[$side] 기기 연결 시도: ${device.name ?: "Unknown"} ($address)")

        val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }

        if (gatt == null) {
            Timber.e("[$side] connectGatt 호출 실패.")
            handleConnectionFailure(null, side, "connectGatt returned null")
        } else {
            if (side == InsoleSide.LEFT) leftGatt = gatt else rightGatt = gatt
        }
    }

    fun disconnectPair() {
        Timber.d("페어 연결 해제 요청")
        leftGatt?.disconnect()
        rightGatt?.disconnect()
    }


    private fun getSideFromGatt(gatt: BluetoothGatt?): InsoleSide? {
        return when (gatt) {
            leftGatt -> InsoleSide.LEFT
            rightGatt -> InsoleSide.RIGHT
            else -> {
                Timber.w("알 수 없는 GATT 객체로부터 콜백 수신: ${gatt?.device?.address}")
                null
            }
        }
    }


    private fun getCurrentDeviceState(side: InsoleSide): DeviceConnectionState {
        return if (side == InsoleSide.LEFT) _leftConnectionState.value else _rightConnectionState.value
    }


    private fun updateDeviceState(side: InsoleSide, newState: DeviceConnectionState) {
        val stateFlow = if (side == InsoleSide.LEFT) _leftConnectionState else _rightConnectionState
        if (stateFlow.value != newState) {
            stateFlow.value = newState
            Timber.d("[$side] 개별 상태 변경 -> $newState")
            // 개별 상태 변경 시 항상 통합 페어 상태 업데이트
            updatePairConnectionStateBasedOnIndividualStates()
        }
    }

    private fun handleConnectionFailure(gatt: BluetoothGatt?, side: InsoleSide, reason: String) {
        Timber.e("[$side] 연결 실패: $reason")
        gatt?.close() // 실패 시 즉시 close
        if (side == InsoleSide.LEFT) leftGatt = null else rightGatt = null
        updateDeviceState(side, DeviceConnectionState.FAILED)
    }


    private fun updatePairConnectionStateBasedOnIndividualStates() {
        val leftState = _leftConnectionState.value
        val rightState = _rightConnectionState.value
        val newPairState = determinePairState(leftState, rightState)

        if (_pairConnectionState.value != newPairState) {
            _pairConnectionState.value = newPairState
            Timber.i("통합 페어 상태 변경 -> $newPairState (Left: $leftState, Right: $rightState)")
        }
    }

    //상태 처리
    private fun determinePairState(left: DeviceConnectionState, right: DeviceConnectionState): InsoleConnectionState {
        return when {
            // 하나라도 실패하면 최종 실패
            left == DeviceConnectionState.FAILED || right == DeviceConnectionState.FAILED -> InsoleConnectionState.FAILED
            // 둘 다 연결 완료
            left == DeviceConnectionState.CONNECTED && right == DeviceConnectionState.CONNECTED -> InsoleConnectionState.FULLY_CONNECTED
            // 둘 다 연결 중 (또는 하나는 연결 중, 다른 하나는 아직 연결 안됨)
            (left == DeviceConnectionState.CONNECTING || right == DeviceConnectionState.CONNECTING) &&
                    (left != DeviceConnectionState.CONNECTED && right != DeviceConnectionState.CONNECTED) && // 아직 둘 다 연결 완료는 아님
                    (left != DeviceConnectionState.FAILED && right != DeviceConnectionState.FAILED) // 실패 상태 아님
            -> InsoleConnectionState.CONNECTING
            // 하나만 연결 완료, 다른 하나는 연결 안됨 (실패나 연결 중 아님)
            (left == DeviceConnectionState.CONNECTED && right == DeviceConnectionState.DISCONNECTED) ||
                    (left == DeviceConnectionState.DISCONNECTED && right == DeviceConnectionState.CONNECTED) ||
                    (left == DeviceConnectionState.CONNECTED && right == DeviceConnectionState.CONNECTING) || // 하나는 연결 완료, 하나는 아직 연결중
                    (left == DeviceConnectionState.CONNECTING && right == DeviceConnectionState.CONNECTED)
            -> InsoleConnectionState.PARTIALLY_CONNECTED // 부분 연결 상태 (이름/정의는 요구사항에 맞게)
            // 둘 다 연결 안됨
            left == DeviceConnectionState.DISCONNECTED && right == DeviceConnectionState.DISCONNECTED -> InsoleConnectionState.DISCONNECTED
            // 그 외의 경우는? (예: 하나는 CONNECTING, 하나는 DISCONNECTED -> CONNECTING으로 처리됨 위에서)
            // 안전하게 기본 상태 설정
            else -> {
                Timber.w("예상치 못한 상태 조합: Left=$left, Right=$right. 기본 상태(DISCONNECTED) 반환.")
                InsoleConnectionState.DISCONNECTED // 또는 FAILED? 정책 결정 필요
            }
        }
    }


    private fun enableSensorNotifications(gatt: BluetoothGatt?, side: InsoleSide) {
        val service = gatt?.getService(BleConstants.SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BleConstants.SENSOR_CHARACTERISTIC_UUID)

        if (characteristic == null) {
            handleConnectionFailure(gatt, side, "센서 캐릭터리스틱(${BleConstants.SENSOR_CHARACTERISTIC_UUID})을 찾을 수 없음.")
            return
        }

        val notificationSet = gatt.setCharacteristicNotification(characteristic, true)
        if (!notificationSet) {
            handleConnectionFailure(gatt, side, "setCharacteristicNotification 실패.")
            return
        }

        val descriptor = characteristic.getDescriptor(BleConstants.CCC_DESCRIPTOR_UUID)
        if (descriptor == null) {
            handleConnectionFailure(gatt, side, "CCCD(${BleConstants.CCC_DESCRIPTOR_UUID})를 찾을 수 없음.")
            return
        }

        val writeSuccess: Boolean = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val result = gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                Timber.d("[$side] writeDescriptor (API 33+) result: $result")
                result == BluetoothStatusCodes.SUCCESS
            } else {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor) // 결과 즉시 반환 (성공 여부는 콜백에서)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "[$side] Descriptor 쓰기 중 권한 오류 발생")
            handleConnectionFailure(gatt, side, "Descriptor write permission denied")
            return // 함수 종료
        }


        if (!writeSuccess && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Timber.e("[$side] Descriptor 쓰기 시작 실패 (API 32-)")
            handleConnectionFailure(gatt, side, "Descriptor write initiation failed (API 32-)")
        } else if(writeSuccess && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Timber.d("[$side] Descriptor 쓰기 시작 성공 (API 32-). onDescriptorWrite 콜백 대기 중...")
        }
        // API 33+ 는 즉시 성공/실패 확인 가능 (콜백도 호출됨)
        // 실패 시 콜백(onDescriptorWrite)에서 처리됨 -> 여기서 추가 실패 처리는 불필요할 수 있음 (중복)
        // 단, writeDescriptor 자체가 false를 반환하면 즉시 실패 처리 필요 (writeSuccess 변수 확인)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !writeSuccess) {
            Timber.e("[$side] Descriptor 쓰기 즉시 실패 (API 33+)")
            // 실패 처리는 onDescriptorWrite 콜백에서 status 코드로 진행될 것임
            // 여기서 중복으로 handleConnectionFailure를 호출하면 상태 관리가 꼬일 수 있음
            // 상태 업데이트는 onDescriptorWrite 콜백에 맡기는 것이 좋음
        }
    }
}

object BleConstants {
    const val DEVICE_L_NAME = "RunMate_SmartInsole_L"
    const val DEVICE_R_NAME = "RunMate_SmartInsole_R"
    val SERVICE_UUID: UUID = UUID.fromString("5fdc7093-3882-416b-bc2b-eb76a749aef3")
    val SENSOR_CHARACTERISTIC_UUID: UUID = UUID.fromString("9b836621-335d-4f1b-9cf5-a898b283beb6")
    val CCC_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    const val REQUEST_MTU_SIZE = 64
    const val RESPONSE_DATA_SIZE = 32


}

private enum class DeviceConnectionState { DISCONNECTED, CONNECTING, CONNECTED, FAILED }