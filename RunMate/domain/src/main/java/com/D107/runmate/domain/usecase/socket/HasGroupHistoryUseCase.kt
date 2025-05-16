package com.D107.runmate.domain.usecase.socket

import com.D107.runmate.domain.repository.group.GroupRepository
import javax.inject.Inject

class HasGroupHistoryUseCase @Inject constructor(
    private val groupRepository: GroupRepository
){
    suspend operator fun invoke() = groupRepository.hasGroupHistory()
}