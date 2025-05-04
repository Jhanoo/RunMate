package com.D107.runmate.watch.data.repository

import android.util.Log
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import com.D107.runmate.watch.data.local.HealthServicesManager
import com.D107.runmate.watch.domain.model.HeartRate
import com.D107.runmate.watch.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateRepositoryImpl @Inject constructor(
    // Health Service를 통해 심박수 데이터를 가져옴
    private val healthServicesManager: HealthServicesManager
) : HeartRateRepository {

    private val _heartRateFlow = MutableStateFlow(HeartRate(0))

    // Health Services 측정 콜백 정의
    private val measureCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
        }

        // 새로운 심박수 데이터가 수신되었을 때 호출됨
        override fun onDataReceived(data: DataPointContainer) {
            val heartRateDataPoints = data.getData(DataType.HEART_RATE_BPM)
            heartRateDataPoints.forEach { dataPoint ->
                val bpm = dataPoint.value.toInt() // 데이터에서 bpm 추출
//                Log.d("sensor","심박수 in RepoImpl : $bpm")
                _heartRateFlow.value = HeartRate(bpm)
            }
        }
    }

    // 외부에서 수신 가능한 심박수 데이터 Flow 반환
    override fun getHeartRateFlow(): Flow<HeartRate> = _heartRateFlow.asStateFlow()

    // 심박수 측정 시작
    override suspend fun startHeartRateMonitoring() {
        healthServicesManager.registerHeartRateCallback(measureCallback)
    }

    // 심박수 측정 중단
    override suspend fun stopHeartRateMonitoring() {
        healthServicesManager.unregisterHeartRateCallback(measureCallback)
    }
}
