package com.D107.runmate.watch.domain.usecase.heartRate

import android.util.Log
import com.D107.runmate.watch.domain.model.HeartRate
import com.D107.runmate.watch.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GetHeartRateUseCaseTest {

    @Mock
    private lateinit var repository: HeartRateRepository

    private lateinit var useCase: GetHeartRateUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetHeartRateUseCase(repository)
    }

    @Test
    fun `invoke should return flow from repository`() = runTest {
        // Given
        val mockHeartRate = HeartRate(bpm = 75)
        val flow = flowOf(mockHeartRate)
        `when`(repository.getHeartRateFlow()).thenReturn(flow)

        // When
        val result = useCase().first()

        // Then
        assertEquals(75, result.bpm)
        verify(repository).getHeartRateFlow()
    }
}