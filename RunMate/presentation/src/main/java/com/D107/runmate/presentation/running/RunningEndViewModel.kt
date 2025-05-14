package com.D107.runmate.presentation.running

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Address
import com.D107.runmate.domain.repository.running.RunningRepository
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.domain.usecase.running.EndRunningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RunningEndViewModel @Inject constructor(
    private val endRunningUseCase: EndRunningUseCase,
    private val getCoord2AddressUseCase: GetCoord2AddressUseCase
) : ViewModel() {
    private val _endRunning = MutableSharedFlow<RunningEndState>()
    val endRunning = _endRunning.asSharedFlow()

    private val _coord2Address = MutableSharedFlow<Coord2AddressState>()
    val coord2Address = _coord2Address.asSharedFlow()

    fun getCoord2Address(lat: Double, lon: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            getCoord2AddressUseCase(lat, lon).collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        _coord2Address.emit(Coord2AddressState.Success(status.data))
                    }

                    is ResponseStatus.Error -> {
                        Timber.e("coord2address error ${status.error}")
                        _coord2Address.emit(Coord2AddressState.Error(status.error.message))
                    }
                }
            }
        }
    }

    fun endRunning(
        avgBpm: Double,
        avgCadence: Double,
        avgElevation: Double,
        avgPace: Double,
        calories: Double,
        courseId: String?,
        distance: Double,
        endTime: String,
        startLocation: String,
        startTime: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            endRunningUseCase(
                avgBpm,
                avgCadence,
                avgElevation,
                avgPace,
                calories,
                courseId,
                distance,
                endTime,
                startLocation,
                startTime
            )
                .onStart {
                }
                .catch { e ->
                    Timber.e("runningend error catch ${e.message}")
                    _endRunning.emit(RunningEndState.Error(e.message ?: "알 수 없는 오류가 발생했습니다"))
                }
                .collect { status ->
                    when (status) {
                        is ResponseStatus.Success -> {
                            Timber.d("runningend success")
                            _endRunning.emit(RunningEndState.Success)
                        }

                        is ResponseStatus.Error -> {
                            Timber.d("runningend error ${status.error.message}")
                            _endRunning.emit(RunningEndState.Error(status.error.message))
                        }
                    }
                }
        }
    }
}

sealed class RunningEndState {
    object Success : RunningEndState()
    data class Error(val message: String) : RunningEndState()
}

sealed class Coord2AddressState {
    data class Success(val address: Address) : Coord2AddressState()
    data class Error(val message: String) : Coord2AddressState()
}