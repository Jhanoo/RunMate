package com.D107.runmate.domain.repository

import com.D107.runmate.domain.model.Insole.CombinedInsoleData
import com.D107.runmate.domain.model.Insole.SmartInsole
import com.D107.runmate.domain.model.Insole.InsoleConnectionState
import com.D107.runmate.domain.model.Insole.InsoleData
import com.D107.runmate.domain.model.base.ResponseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SmartInsoleRepository {
    fun scanInsole(): Flow<List<SmartInsole>>
    fun connect(leftAddress: String, rightAddress: String)
    fun disconnect()
    fun observeConnectionState(): StateFlow<InsoleConnectionState>
    fun observeCombinedInsoleData(): Flow<ResponseStatus<CombinedInsoleData>>
}