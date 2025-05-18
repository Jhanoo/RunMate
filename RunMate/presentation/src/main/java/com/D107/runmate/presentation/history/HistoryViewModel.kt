package com.D107.runmate.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseDetail
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.history.History
import com.D107.runmate.domain.model.history.HistoryDetail
import com.D107.runmate.domain.model.history.HistoryInfo
import com.D107.runmate.domain.usecase.history.GetHistoryDetailUseCase
import com.D107.runmate.domain.usecase.history.GetHistoryListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryListUseCase: GetHistoryListUseCase,
    private val getHistoryDetailUseCase: GetHistoryDetailUseCase
) : ViewModel() {
    private val _historyList = MutableStateFlow<HistoryListState>(HistoryListState.Initial)
    val historyList = _historyList.asStateFlow()

    private val _historyDetail = MutableStateFlow<HistoryDetailState>(HistoryDetailState.Initial)
    val historyDetail = _historyDetail.asStateFlow()

    fun getHistoryList() {
        viewModelScope.launch {
            getHistoryListUseCase().collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getHistoryList Success {${status.data}}")
                        _historyList.value = HistoryListState.Success(status.data)
                    }
                    is ResponseStatus.Error -> {
                        Timber.d("getHistoryList Error {${status.error}}")
                        _historyList.value = HistoryListState.Error(status.error.message)
                    }
                }
            }
        }
    }

    fun getHistoryDetail(historyId: String) {
        viewModelScope.launch {
            getHistoryDetailUseCase(historyId).collectLatest { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getHistoryDetail Success {${status.data}}")
                        _historyDetail.value = HistoryDetailState.Success(status.data)
                    }
                    is ResponseStatus.Error -> {
                        Timber.d("getHistoryDetail Error {${status.error}}")
                        _historyDetail.value = HistoryDetailState.Error(status.error.message)
                    }
                }
            }
        }
    }
}

sealed class HistoryListState {
    object Initial : HistoryListState()
    data class Success(val historyInfo: HistoryInfo) : HistoryListState()
    data class Error(val message: String?) : HistoryListState()
}

sealed class HistoryDetailState {
    object Initial : HistoryDetailState()
    data class Success(val historyDetail: HistoryDetail) : HistoryDetailState()
    data class Error(val message: String?) : HistoryDetailState()
}