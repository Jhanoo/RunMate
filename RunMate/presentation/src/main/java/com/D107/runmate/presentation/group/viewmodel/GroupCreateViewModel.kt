package com.D107.runmate.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.usecase.group.SearchPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GroupCreateViewModel @Inject constructor(
    private val searchPlaceUseCase: SearchPlaceUseCase
):ViewModel() {
    private val _queryResult = MutableStateFlow<List<Place>>(emptyList())
    val queryResult = _queryResult.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _selectedDate = MutableStateFlow<Date?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private var currentQuery: String = ""



    fun searchPlace(query: String) {
        viewModelScope.launch {
            searchPlaceUseCase(query).collect { response ->
                when (response) {
                    is ResponseStatus.Success -> {
                        _queryResult.value = response.data
                    }

                    is ResponseStatus.Error -> {

                    }
                }
            }
        }
    }

    fun selectPlace(place:Place){
        _selectedPlace.value = place
    }

    fun selectDate(date:Date){
        _selectedDate.value = date
    }

    fun clearQuery() {
        currentQuery = ""
    }

    fun clearResult() {
        currentQuery = ""
        _queryResult.value = emptyList()
    }

}