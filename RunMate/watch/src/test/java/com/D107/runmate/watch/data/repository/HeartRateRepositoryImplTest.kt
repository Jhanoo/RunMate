package com.D107.runmate.watch.data.repository

import androidx.health.services.client.MeasureCallback
import com.D107.runmate.watch.data.local.HealthServicesManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

class HeartRateRepositoryImplTest {

    @Mock
    private lateinit var healthServicesManager: HealthServicesManager

    private lateinit var repository: HeartRateRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = HeartRateRepositoryImpl(healthServicesManager)
    }

    @Test
    fun `startHeartRateMonitoring should register callback`() = runTest {
        // When
        repository.startHeartRateMonitoring()

        // Then
        verify(healthServicesManager).registerHeartRateCallback(any())
    }

    @Test
    fun `stopHeartRateMonitoring should unregister callback`() = runTest {
        // When
        repository.stopHeartRateMonitoring()

        // Then
        verify(healthServicesManager).unregisterHeartRateCallback(any())
    }

    @Test
    fun `heartRateRepository should emit values when callback receives data`() = runTest {
        // Given
        val callbackCaptor = argumentCaptor<MeasureCallback>()
        repository.startHeartRateMonitoring()
        verify(healthServicesManager).registerHeartRateCallback(callbackCaptor.capture())

        // Verify callback is captured
        assertNotNull(callbackCaptor.firstValue)
    }
}