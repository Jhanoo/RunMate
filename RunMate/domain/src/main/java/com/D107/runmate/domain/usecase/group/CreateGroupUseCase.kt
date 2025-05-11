package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.repository.group.GroupRepository
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(private val groupRepository: GroupRepository)
{
    suspend operator fun invoke(groupCreateInfo: GroupCreateInfo) = groupRepository.createGroup(groupCreateInfo)

}