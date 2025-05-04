package com.D107.runmate.watch.domain.usecase.timer

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerUseCaseTest {

    private lateinit var startTimerUseCase: StartTimerUseCase
    private lateinit var formatTimeUseCase: FormatTimeUseCase

    @Before
    fun setup() {
        startTimerUseCase = StartTimerUseCase()
        formatTimeUseCase = FormatTimeUseCase()
    }

    @Test
    fun `formatTimeUseCase should format milliseconds correctly`() {
        // FormatTimeUseCase가 올바르게 시간을 문자열로 변환하는지 검증
        assertEquals("0:00:00", formatTimeUseCase(0L))
        assertEquals("0:00:01", formatTimeUseCase(1000L))
        assertEquals("0:01:00", formatTimeUseCase(60000L))
        assertEquals("1:00:00", formatTimeUseCase(3600000L))
        assertEquals("1:23:45", formatTimeUseCase(5025000L))
    }

    @Test
    fun `startTimerUseCase should emit elapsed time every second`() = runTest {
        // 1초마다 흐른 시간을 emit하는지 검증
        val emissions = mutableListOf<Long>()

        startTimerUseCase(0L, 0L) { /* onTick 무시 */ }
            .take(3) // 처음 3개 값만 수집
            .collect { elapsed ->
                emissions.add(elapsed)
            }

        // 3개의 값이 방출되었고 시간이 증가하는지 확인
        assertEquals(3, emissions.size)
        assertTrue(emissions[0] < emissions[1])
        assertTrue(emissions[1] < emissions[2])
    }


    @Test
    fun `startTimerUseCase should continue from paused time`() = runTest {
        // pause 상태에서 재시작했을 때 누적 시간에서 이어지는지 검증
        val emissions = mutableListOf<Long>()
        val pausedTime = 5000L

        startTimerUseCase(0L, pausedTime) { /* onTick 무시 */ }
            .take(2) // 처음 2개 값만 수집
            .collect { elapsed ->
                emissions.add(elapsed)
            }

        // 방출된 모든 값이 pausedTime 이상인지 확인 (시간이 이어졌는가)
        assertTrue(emissions.all { it >= pausedTime })
    }
}