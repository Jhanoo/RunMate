package com.D107.runmate.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseFilter
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.usecase.course.GetAllCourseUseCase
import com.D107.runmate.domain.usecase.course.GetRecentCourseUseCase
import com.D107.runmate.domain.usecase.course.SearchCourseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val getAllCourseUseCase: GetAllCourseUseCase,
    private val searchCourseUseCase: SearchCourseUseCase,
    private val getMyCourseUseCase: GetAllCourseUseCase,
    private val getRecentCourseUseCase: GetRecentCourseUseCase
) : ViewModel() {
    private val _courseList = MutableStateFlow<CourseSearchState>(CourseSearchState.Initial)
    val courseList = _courseList.asStateFlow()

    private val _myCourseList = MutableStateFlow<CourseSearchState>(CourseSearchState.Initial)
    val myCourseList = _myCourseList.asStateFlow()

    private val _recentCourseList = MutableStateFlow<CourseSearchState>(CourseSearchState.Initial)
    val recentCourseList = _recentCourseList.asStateFlow()

    private val _courseFilter = MutableStateFlow<CourseFilterState>(CourseFilterState.Initial)
    val courseFilter = _courseFilter.asStateFlow()

    fun getAllCourseList() {
        viewModelScope.launch {
            getAllCourseUseCase().collect { status ->
                when(status) {
                    is ResponseStatus.Success -> Timber.d("getAllCourseList Success {${status.data}}")
                    is ResponseStatus.Error -> Timber.d("getAllCourseList Error {${status.error}}")
                }
            }
        }
    }

    fun searchCourse(keyword: String) {
        viewModelScope.launch {
            searchCourseUseCase(keyword).collect { status ->
                when(status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getAllCourseList Success {${status.data}}")
                        _courseList.value = CourseSearchState.Success(status.data)
                    }
                    is ResponseStatus.Error -> {
                        Timber.d("getAllCourseList Error {${status.error}}")
                        _courseList.value = CourseSearchState.Error(status.error.message)
                    }
                }
            }
        }
    }

    fun getMyCourse() {
        viewModelScope.launch {
            getMyCourseUseCase().collect { status ->
                when(status) {
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

    fun getRecentCourse() {
        viewModelScope.launch {
            getRecentCourseUseCase().collect { status ->
                when(status) {
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

    fun setCourseFilter(state: CourseFilterState) {
        _courseFilter.value = state
    }
}

sealed class CourseSearchState {
    object Initial: CourseSearchState()
    data class Success(val courseList: List<CourseInfo>): CourseSearchState()
    data class Error(val message: String) : CourseSearchState()
}

sealed class CourseFilterState {
    object Initial: CourseFilterState()
    data class Set(val filter: CourseFilter): CourseFilterState()
}