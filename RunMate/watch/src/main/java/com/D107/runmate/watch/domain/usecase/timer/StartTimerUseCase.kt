package com.D107.runmate.watch.domain.usecase.timer

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject

class StartTimerUseCase @Inject constructor(){
    operator fun invoke(
        startTime: Long,
        pauseTime: Long,
        onTick: (Long) -> Unit
    ) : Flow<Long> = flow {
        var elapsed = pauseTime

        val actualStartTime = if (startTime == 0L) {
            System.currentTimeMillis()
        } else {
            System.currentTimeMillis() - pauseTime
        }

        while (currentCoroutineContext().isActive) {
            emit(elapsed)
            delay(1000)
            elapsed += 1000
        }
    }
}