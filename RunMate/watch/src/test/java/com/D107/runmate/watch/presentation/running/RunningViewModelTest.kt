package com.D107.runmate.watch.presentation.running

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.D107.runmate.watch.domain.model.HeartRate
import com.D107.runmate.watch.domain.usecase.heartRate.GetHeartRateUseCase
import com.D107.runmate.watch.domain.usecase.heartRate.StartHeartRateMonitoringUseCase
import com.D107.runmate.watch.domain.usecase.heartRate.StopHeartRateMonitoringUseCase
import com.D107.runmate.watch.domain.usecase.timer.FormatTimeUseCase
import com.D107.runmate.watch.domain.usecase.timer.StartTimerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class RunningViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var getHeartRateUseCase: GetHeartRateUseCase

    @Mock
    private lateinit var startHeartRateMonitoringUseCase: StartHeartRateMonitoringUseCase

    @Mock
    private lateinit var stopHeartRateMonitoringUseCase: StopHeartRateMonitoringUseCase

    @Mock
    private lateinit var startTimerUseCase: StartTimerUseCase

    val formatTimeUseCase: FormatTimeUseCase = mock()

    private lateinit var viewModel: RunningViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should start heart rate monitoring`() = runTest {
        // Given
        `when`(getHeartRateUseCase()).thenReturn(flowOf(HeartRate(0)))

        // When
        viewModel = RunningViewModel(
            getHeartRateUseCase,
            startHeartRateMonitoringUseCase,
            stopHeartRateMonitoringUseCase,
            startTimerUseCase,
            formatTimeUseCase
        )
        advanceUntilIdle()

        // Then
        verify(startHeartRateMonitoringUseCase).invoke()
    }

    @Test
    fun `heart rate should update when new data received`() = runTest {
        // Given
        val heartRateFlow = MutableSharedFlow<HeartRate>()
        `when`(getHeartRateUseCase()).thenReturn(heartRateFlow)

        // When
        viewModel = RunningViewModel(
            getHeartRateUseCase,
            startHeartRateMonitoringUseCase,
            stopHeartRateMonitoringUseCase,
            startTimerUseCase,
            formatTimeUseCase
        )
        advanceUntilIdle()

        heartRateFlow.emit(HeartRate(85))
        advanceUntilIdle()

        // Then
        assertEquals(85, viewModel.heartRate.value)
    }

    @Test
    fun `startMonitoring should call use case`() = runTest {
        // Given
        `when`(getHeartRateUseCase()).thenReturn(flowOf(HeartRate(0)))
        viewModel = RunningViewModel(
            getHeartRateUseCase,
            startHeartRateMonitoringUseCase,
            stopHeartRateMonitoringUseCase,
            startTimerUseCase,
            formatTimeUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.startMonitoring()
        advanceUntilIdle()

        // Then
        verify(startHeartRateMonitoringUseCase).invoke()
    }

    @Test
    fun `stopMonitoring should call use case`() = runTest {
        // Given
        `when`(getHeartRateUseCase()).thenReturn(flowOf(HeartRate(0)))
        viewModel = RunningViewModel(
            getHeartRateUseCase,
            startHeartRateMonitoringUseCase,
            stopHeartRateMonitoringUseCase,
            startTimerUseCase,
            formatTimeUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.stopMonitoring()
        advanceUntilIdle()

        // Then
        verify(stopHeartRateMonitoringUseCase).invoke()
    }

    @Test
    fun `multiple heart rate updates should be reflected in state`() = runTest {
        // Given
        val heartRateFlow = MutableSharedFlow<HeartRate>()
        `when`(getHeartRateUseCase()).thenReturn(heartRateFlow)

        // When
        viewModel = RunningViewModel(
            getHeartRateUseCase,
            startHeartRateMonitoringUseCase,
            stopHeartRateMonitoringUseCase,
            startTimerUseCase,
            formatTimeUseCase
        )
        advanceUntilIdle()

        heartRateFlow.emit(HeartRate(72))
        advanceUntilIdle()
        assertEquals(72, viewModel.heartRate.value)

        heartRateFlow.emit(HeartRate(85))
        advanceUntilIdle()
        assertEquals(85, viewModel.heartRate.value)

        heartRateFlow.emit(HeartRate(90))
        advanceUntilIdle()
        assertEquals(90, viewModel.heartRate.value)
    }
}