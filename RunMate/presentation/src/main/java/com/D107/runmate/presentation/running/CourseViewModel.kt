package com.D107.runmate.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseDetail
import com.D107.runmate.domain.model.course.CourseFilter
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.usecase.course.CreateCourseUseCase
import com.D107.runmate.domain.usecase.course.GetAllCourseUseCase
import com.D107.runmate.domain.usecase.course.GetCourseDetailUseCase
import com.D107.runmate.domain.usecase.course.GetInputStreamFromUrlUseCase
import com.D107.runmate.domain.usecase.course.GetMyCourseUseCase
import com.D107.runmate.domain.usecase.course.GetRecentCourseUseCase
import com.D107.runmate.domain.usecase.course.SearchCourseUseCase
import com.D107.runmate.domain.usecase.course.UpdateCourseLikeUseCase
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.presentation.utils.GpxParser.getGpxInputStream
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.kakao.vectormap.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val getAllCourseUseCase: GetAllCourseUseCase,
    private val searchCourseUseCase: SearchCourseUseCase,
    private val getMyCourseUseCase: GetMyCourseUseCase,
    private val getRecentCourseUseCase: GetRecentCourseUseCase,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val getInputStreamFromUrlUseCase: GetInputStreamFromUrlUseCase,
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseLikeUseCase: UpdateCourseLikeUseCase,
    private val coord2AddressUseCase: GetCoord2AddressUseCase
) : ViewModel() {
    private val _courseList = MutableStateFlow<CourseSearchState>(CourseSearchState.Initial)
    val courseList = _courseList.asStateFlow()

    private val _myCourseList = MutableStateFlow<CourseSearchState>(CourseSearchState.Initial)
    val myCourseList = _myCourseList.asStateFlow()

    private val _recentCourseList = MutableStateFlow<CourseSearchState>(CourseSearchState.Initial)
    val recentCourseList = _recentCourseList.asStateFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailState>(CourseDetailState.Initial)
    val courseDetail = _courseDetail.asStateFlow()

    private val _address = MutableStateFlow<String?>(null)
    val address = _address.asStateFlow()

    fun getAllCourseList() {
        viewModelScope.launch {
            getAllCourseUseCase().collect { status ->
                when (status) {
                    is ResponseStatus.Success -> Timber.d("getAllCourseList Success {${status.data}}")
                    is ResponseStatus.Error -> Timber.d("getAllCourseList Error {${status.error}}")
                }
            }
        }
    }

    fun searchCourse(keyword: String) {
        viewModelScope.launch {
            searchCourseUseCase(keyword).collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("searchCourse Success {${status.data}}")
                        _courseList.value = CourseSearchState.Success(status.data)
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("searchCourse Error {${status.error}}")
                        _courseList.value = CourseSearchState.Error(status.error.message)
                    }
                }
            }
        }
    }

    fun getMyCourse() {
        viewModelScope.launch {
            getMyCourseUseCase().collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getMyCourse Success {${status.data}}")
                        _myCourseList.value = CourseSearchState.Success(status.data)
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("getMyCourse Error {${status.error}}")
                        _myCourseList.value = CourseSearchState.Error(status.error.message)
                    }
                }
            }
        }
    }

    fun getRecentCourse() {
        viewModelScope.launch {
            getRecentCourseUseCase().collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getAllCourseList Success {${status.data}}")
                        _myCourseList.value = CourseSearchState.Success(status.data)
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("getAllCourseList Error {${status.error}}")
                        _myCourseList.value = CourseSearchState.Error(status.error.message)
                    }
                }
            }
        }
    }

    fun getCourseDetail(courseId: String) {
        viewModelScope.launch {
            getCourseDetailUseCase(courseId).collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getCourseDetail Success {${status.data}}")
                        _courseDetail.value = CourseDetailState.Success(status.data)
//                        withContext(Dispatchers.IO) {
//                            getInputStreamFromUrlUseCase(status.data.gpxFile).collectLatest {
//                                Timber.d("getInputStream Before")
//                                try{
//                                    val trackPointList = parseGpx(it)
//                                    Timber.d("getInputStream Success {${trackPointList}}")
//                                    _courseDetail.value = CourseDetailState.Success(status.data)
//                                }catch (e: Exception) {
//                                    Timber.e("getInputStream Error {${e.message}}")
//                                }
//
//                            }
//                        }
                    }

                    is ResponseStatus.Error -> Timber.d("getCourseDetail Error {${status.error}}")
                }
            }
        }
    }

    fun createCourse(
        avgElevation: Double,
        distance: Float,
        historyId: String,
        name: String,
        shared: Boolean,
        startLocation: String
    ) {
        viewModelScope.launch {
            createCourseUseCase(avgElevation, distance, historyId, name, shared, startLocation).collectLatest {

            }
        }
    }

    fun updateCourseLike(courseId: String) {
        viewModelScope.launch {
            updateCourseLikeUseCase(courseId).collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("updateCourseLike Success {${status.data}}")
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("updateCourseLike Error {${status.error}}")
                    }
                }
            }
        }
    }

    fun getAddressFromLatLng(longitude: Double, latitude: Double) {
        viewModelScope.launch {
            coord2AddressUseCase(longitude, latitude).collect { response ->
                when (response) {
                    is ResponseStatus.Success -> {
                        Timber.d("getAddressFromLatLng Success {${response.data}}")
                        _address.value = response.data.address_name
                    }
                    is ResponseStatus.Error -> {
                        Timber.d("getAddressFromLatLng Error {${response.error}}")
                        _address.value = null
                    }
                }
            }
        }
    }
}

sealed class CourseSearchState {
    object Initial : CourseSearchState()
    data class Success(val courseList: List<CourseInfo>) : CourseSearchState()
    data class Error(val message: String) : CourseSearchState()
}

sealed class CourseDetailState {
    object Initial : CourseDetailState()
    data class Success(val courseDetail: CourseDetail) : CourseDetailState()
    data class Error(val message: String) : CourseDetailState()
}