package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.repository.group.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LeaveGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
)  {
    suspend operator fun invoke() : Flow<ResponseStatus<Unit>> = groupRepository.leaveGroup()
}