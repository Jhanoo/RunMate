package com.D107.runmate.watch.data.local

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.DataType
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.unregisterMeasureCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthServicesManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val healthClient = HealthServices.getClient(context)
    private val measureClient = healthClient.measureClient

    suspend fun registerHeartRateCallback(callback: MeasureCallback) {
        val capabilities = measureClient.getCapabilities()
        if (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure) {
            measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
        }
    }

    suspend fun unregisterHeartRateCallback(callback: MeasureCallback) {
        measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, callback)
    }

}