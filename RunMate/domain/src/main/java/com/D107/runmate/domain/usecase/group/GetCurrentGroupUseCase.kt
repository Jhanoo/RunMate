package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.GroupData
import com.D107.runmate.domain.repository.group.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(): Flow<ResponseStatus<GroupData?>> = groupRepository.getCurrentGroup()

}