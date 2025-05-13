package com.D107.runmate.watch.presentation.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(deviceAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (bluetoothSocket?.isConnected == true) {
                    bluetoothSocket?.close()
                }

                val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                bluetoothSocket = device?.createRfcommSocketToServiceRecord(SERVICE_UUID)
                bluetoothSocket?.connect()

                connectedDeviceAddress = deviceAddress
                Log.d(TAG, "Successfully connected to device: $deviceAddress")
                true
            } catch (e: IOException) {
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

    fun disconnect() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            connectedDeviceAddress = null
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket: ${e.message}")
        }
    }
}