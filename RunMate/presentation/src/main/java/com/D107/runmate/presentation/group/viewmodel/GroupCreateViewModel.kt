package com.D107.runmate.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.usecase.group.CreateGroupUseCase
import com.D107.runmate.domain.usecase.group.SearchPlaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GroupCreateViewModel @Inject constructor(
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val createGroupUseCase: CreateGroupUseCase
):ViewModel() {
    private val _queryResult = MutableStateFlow<List<Place>>(emptyList())
    val queryResult = _queryResult.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _selectedDate = MutableStateFlow<OffsetDateTime?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private var currentQuery: String = ""


    fun createGroup() {
        viewModelScope.launch {
            Timber.d("here!")
            val selectedPlace = selectedPlace.value
            val selectedDate = selectedDate.value
            createGroupUseCase(
                GroupCreateInfo(
                    groupName = "test",
                    courseId = null,
                    startTime = selectedDate!!,
                    startLocation = selectedPlace!!.address,
                    latitude = selectedPlace!!.x,
                    longitude = selectedPlace!!.y
                )

            )
        }
    }


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

    fun selectDate(date: OffsetDateTime){
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