package com.D107.runmate.watch.domain.usecase.heartRate

import android.util.Log
import com.D107.runmate.watch.domain.model.HeartRate
import com.D107.runmate.watch.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class GetHeartRateUseCase @Inject constructor(
    private val heartRateRepository: HeartRateRepository
) {
    operator fun invoke(): Flow<HeartRate> {
        return heartRateRepository.getHeartRateFlow()
            .onEach { heartRate ->
//                Log.d("sensor", "UseCase에서 전달하는 심박수: ${heartRate.bpm}")
            }
    }
}