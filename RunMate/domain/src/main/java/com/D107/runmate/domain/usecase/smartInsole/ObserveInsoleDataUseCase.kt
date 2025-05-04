package com.D107.runmate.domain.usecase.smartInsole

import com.D107.runmate.domain.model.Insole.CombinedInsoleData
import com.D107.runmate.domain.model.Insole.InsoleData
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.repository.SmartInsoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// 인솔 데이터 실시간 관찰
class ObserveInsoleDataUseCase @Inject constructor(
    private val smartInsoleRepository: SmartInsoleRepository
) {
    operator fun invoke(): Flow<ResponseStatus<CombinedInsoleData>> = smartInsoleRepository.observeCombinedInsoleData()

}