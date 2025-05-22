package com.D107.runmate.domain.usecase.group

import com.D107.runmate.domain.repository.group.GroupRepository
import javax.inject.Inject

class FinishGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
){
    suspend operator fun invoke() = groupRepository.finishGroup()

}