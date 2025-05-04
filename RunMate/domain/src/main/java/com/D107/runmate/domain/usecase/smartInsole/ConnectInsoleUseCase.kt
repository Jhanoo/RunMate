package com.D107.runmate.domain.usecase.smartInsole

import com.D107.runmate.domain.repository.SmartInsoleRepository
import javax.inject.Inject

class ConnectInsoleUseCase @Inject constructor(
    private val smartInsoleRepository: SmartInsoleRepository
) {
    operator fun invoke(leftAddress:String,rightAddress:String) = smartInsoleRepository.connect(leftAddress,rightAddress)

}