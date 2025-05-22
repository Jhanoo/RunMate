package com.D107.runmate.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.manager.TodoItem
import com.D107.runmate.domain.usecase.manager.GetDailyTodoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DailyTodoViewModel @Inject constructor(
    private val dailyTodoUseCase: GetDailyTodoUseCase
): ViewModel() {
    private val _dailyTodo = MutableStateFlow<DailyTodoState>(DailyTodoState.Initial)
    val dailyTodo = _dailyTodo.asStateFlow()

    fun getDailyTodo() {
        viewModelScope.launch {
            dailyTodoUseCase().collectLatest { status ->
                when(status) {
                    is ResponseStatus.Success -> {
                        Timber.d("getDailyTodo Success {${status.data}}")
                        _dailyTodo.value = DailyTodoState.Success(status.data)
                    }
                    is ResponseStatus.Error -> {
                        Timber.d("getDailyTodo Error {${status.error}}")
                        _dailyTodo.value = DailyTodoState.Error(status.error.message)
                    }
                }
            }
        }
    }
}

sealed class DailyTodoState {
    object Initial : DailyTodoState()
    data class Success(val todo: TodoItem) : DailyTodoState()
    data class Error(val message: String) : DailyTodoState()
}