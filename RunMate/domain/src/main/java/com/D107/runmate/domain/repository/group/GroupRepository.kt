package com.D107.runmate.domain.repository.group

import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.model.group.GroupData
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun createGroup(groupCreateInfo: GroupCreateInfo): GroupData?
}