package com.D107.runmate.domain.repository.group

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.model.group.GroupData
import com.D107.runmate.domain.model.group.JoinInfo
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun createGroup(groupCreateInfo: GroupCreateInfo): Flow<ResponseStatus<GroupData?>>
    suspend fun getCurrentGroup(): Flow<ResponseStatus<GroupData?>>
    suspend fun leaveGroup(): Flow<ResponseStatus<Unit>>
    suspend fun joinGroup(intviteCode:String): Flow<ResponseStatus<JoinInfo?>>
    suspend fun startGroup():Flow<ResponseStatus<Unit?>>
    suspend fun finishGroup():Flow<ResponseStatus<Unit?>>
}