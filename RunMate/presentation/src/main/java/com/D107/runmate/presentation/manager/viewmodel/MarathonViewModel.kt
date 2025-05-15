package com.D107.runmate.presentation.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.manager.MarathonInfo
import com.D107.runmate.domain.usecase.manager.GetMarathonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class MarathonViewModel @Inject constructor(
    private val getMarathonsUseCase: GetMarathonsUseCase
) : ViewModel() {

    private val _marathons = MutableLiveData<List<MarathonInfo>>()
    val marathons: LiveData<List<MarathonInfo>> = _marathons

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _selectedDate = MutableStateFlow<OffsetDateTime?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private var allMarathons = listOf<MarathonInfo>()

    fun getMarathons() {
        viewModelScope.launch {
            _isLoading.value = true
            getMarathonsUseCase.invoke()
                .catch { e ->
                    _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
                    _isLoading.value = false
                }
                .collect { result ->
                    _isLoading.value = false
                    result.onSuccess { marathons ->
                        allMarathons = marathons
                        _marathons.value = marathons
                    }.onFailure { e ->
                        _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
                    }
                }
        }
    }

    fun searchMarathons(query: String) {
        if (query.isEmpty()) {
            _marathons.value = allMarathons
            return
        }

        _marathons.value = allMarathons.filter { marathon ->
            marathon.title.contains(query, ignoreCase = true) ||
                    marathon.location.contains(query, ignoreCase = true)
        }
    }

    fun selectDate(date: OffsetDateTime){
        _selectedDate.value = date
    }
}