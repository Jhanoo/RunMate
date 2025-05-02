package com.D107.runmate.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    private val _userLocation = MutableStateFlow<UserLocationState>(UserLocationState.Initial)
    val userLocation = _userLocation.asStateFlow()

    fun setUserLocation(state: UserLocationState) {
        _userLocation.value = state
    }
}

sealed class UserLocationState {
    object Initial: UserLocationState()
    data class Exist(val location: Location): UserLocationState()
}