package com.D107.runmate.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseDetail
import com.D107.runmate.domain.model.group.Address
import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.model.group.Place
import com.D107.runmate.domain.model.group.RoadAddress
import com.D107.runmate.domain.model.running.TrackPoint
import com.D107.runmate.domain.usecase.group.CreateGroupUseCase
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.domain.usecase.group.SearchPlaceUseCase
import com.D107.runmate.presentation.utils.GpxParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class GroupCreateViewModel @Inject constructor(
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val getCoord2AddressUseCase: GetCoord2AddressUseCase
) : ViewModel() {
    private val _queryResult = MutableStateFlow<List<Place>>(emptyList())
    val queryResult = _queryResult.asStateFlow()

    private val _groupName = MutableStateFlow<String>("")
    val groupName = _groupName.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseDetail?>(null)
    val selectedCourse = _selectedCourse.asStateFlow()

    private val _selectedPlace = MutableStateFlow<Place?>(null)
    val selectedPlace = _selectedPlace.asStateFlow()

    private val _selectedDate = MutableStateFlow<OffsetDateTime?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    private val _addressResult = MutableStateFlow<Address?>(null)
    val addressResult = _addressResult.asStateFlow()

    private val _longitude = MutableStateFlow<Double>(0.0)
    val longitude = _longitude.asStateFlow()

    private val _latitude = MutableStateFlow<Double>(0.0)
    val latitude = _latitude.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GroupUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()



    private var currentQuery: String = ""


    fun createGroup() {
        viewModelScope.launch {
            if (groupName.value.isBlank()) {
                _uiEvent.emit(GroupUiEvent.ShowToast("그룹 이름을 입력해주세요."))
                return@launch
            }
            if (groupName.value.length > 12) {
                _uiEvent.emit(GroupUiEvent.ShowToast("그룹 이름은 12자 이하로 입력해주세요."))
                return@launch
            }
            if (selectedPlace.value == null) {
                _uiEvent.emit(GroupUiEvent.ShowToast("장소를 선택해주세요."))
                return@launch
            }
            if (selectedDate.value == null) {
                _uiEvent.emit(GroupUiEvent.ShowToast("날짜를 선택해주세요."))
                return@launch
            }
            Timber.d("here!")
            val selectedPlace = selectedPlace.value
            val selectedDate = selectedDate.value
            createGroupUseCase(
                GroupCreateInfo(
                    groupName = groupName.value,
                    courseId = selectedCourse.value?.id,
                    startTime = selectedDate!!,
                    startLocation = selectedPlace!!.address,
                    latitude = selectedPlace!!.x,
                    longitude = selectedPlace!!.y
                )

            ).collect { result ->
                if (result is ResponseStatus.Success) {
                    Timber.d("CreateGroup Success")
                    _uiEvent.emit(GroupUiEvent.CreationSuccess)

                } else if (result is ResponseStatus.Error) {
                    Timber.d("CreateGroup Fail")
                    _uiEvent.emit(GroupUiEvent.ShowToast("그룹 생성에 실패 하였습니다."))
                }

            }
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

    fun getCoord2Address(x: Double, y: Double) {
        viewModelScope.launch {
            getCoord2AddressUseCase(x, y).collect { response ->
                when (response) {
                    is ResponseStatus.Success -> {
                        _addressResult.value = response.data
                        _latitude.value = y
                        _longitude.value = x
                    }

                    is ResponseStatus.Error -> {
                        Timber.e("coord2address error ${response.error}")
                    }
                }
            }
        }
    }

    fun onGroupNameChanged(name: String) {
        _groupName.value = name
    }

    fun selectPlace(place: Place) {
        _selectedPlace.value = place
    }

    fun selectPlaceOnMap(address:String,latitude:Double, longitude:Double){
        _selectedPlace.value = Place(
            id = "",
            name = "",
            x = longitude,
            y = latitude,
            address = address
        )
    }

    fun selectDate(date: OffsetDateTime) {
        _selectedDate.value = date
    }

    fun setCourse(course:CourseDetail){
        _selectedCourse.value = course

        _selectedPlace.value = Place(
            id = "",
            name = course.name,
            x = 126.896495,
            y = 37.533422,
            address = course.startLocation
        )
        if (course.gpxFile.isNotEmpty()) {
            viewModelScope.launch {
                var firstTrackPoint: TrackPoint? = null

                try {
                    val inputStream = withContext(Dispatchers.IO) {
                        GpxParser.getGpxInputStream(course.gpxFile) // 기존 함수 사용
                    }

                    if (inputStream != null) {
                        val trackPoints = withContext(Dispatchers.Default) { // CPU 바운드 작업이면 Default
                            GpxParser.parseGpx(inputStream)
                        }
                        inputStream.close() // 스트림 닫기 중요
                        firstTrackPoint = trackPoints.firstOrNull()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error processing GPX file in ViewModel for course: ${course.name}")
                    // 에러 처리 (예: 사용자에게 알림, 로그 기록 등)
                }

                firstTrackPoint?.let { point ->
                    _selectedPlace.value = _selectedPlace.value?.copy(
                        x = point.lon,
                        y = point.lat
                    ) ?: Place(
                        id = "",
                        name = course.name,
                        x = point.lon,
                        y = point.lat,
                        address = course.startLocation // 또는 ""
                    )
                } ?: run {
                    Timber.w("Could not get first track point from GPX: ${course.gpxFile}")
                }
            }
        }
    }



    fun clearQuery() {
        currentQuery = ""
    }

    fun clearResult() {
        currentQuery = ""
        _queryResult.value = emptyList()
    }

    fun clearCourse(){
        _selectedCourse.value = null

    }

    fun clearSelectedData(){
        clearQuery()
        clearResult()
        _longitude.value = 0.0
        _latitude.value = 0.0
        _groupName.value = ""
        _selectedDate.value = null
        _selectedPlace.value = null
        _addressResult.value = null
        _selectedCourse.value = null


    }


}