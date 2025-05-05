package com.D107.runmate.watch.domain.repository

import com.D107.runmate.watch.domain.model.HeartRate
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {
    fun getHeartRateFlow(): Flow<HeartRate>
    suspend fun startHeartRateMonitoring()
    suspend fun stopHeartRateMonitoring()
}