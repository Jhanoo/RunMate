package com.D107.runmate.watch.domain.repository

import com.D107.runmate.watch.domain.model.Distance
import kotlinx.coroutines.flow.Flow

interface DistanceRepository {
    fun getDistanceFlow(): Flow<Distance>
    suspend fun startDistanceMonitoring()
    suspend fun stopDistanceMonitoring()
    fun resetDistance()
}