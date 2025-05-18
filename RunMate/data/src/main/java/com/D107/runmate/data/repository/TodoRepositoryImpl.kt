package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.manager.TodoDataSource
import com.D107.runmate.data.remote.response.course.CourseItemResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.response.manager.DailyTodoResponse.Companion.toDomainModel
import com.D107.runmate.domain.model.base.NetworkError
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.manager.TodoItem
import com.D107.runmate.domain.repository.manager.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val todoDataSource: TodoDataSource
) : TodoRepository {
    override suspend fun getTodoList(year: Int, month: Int): Flow<Result<List<TodoItem>>> = flow {
        when (val response = todoDataSource.getTodoList(year, month)) {
            is ApiResponse.Success -> {
                val todoItems = response.data.map { todoResponse ->
                    TodoItem(
                        todoId = todoResponse.todoId,
                        content = todoResponse.content,
                        isDone = todoResponse.isDone,
                        date = todoResponse.date
                    )
                }
                emit(Result.success(todoItems))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception(response.error.message)))
            }
        }
    }

    override suspend fun getDailyTodo(): Flow<ResponseStatus<TodoItem>> {
        return flow {
            try {
                when (val response = todoDataSource.getTodayTodo()) {
                    is ApiResponse.Error -> emit(
                        ResponseStatus.Error(
                            NetworkError(
                                error = response.error.error ?: "UNKNOWN_ERROR",
                                code = response.error.code ?: "UNKNOWN_CODE",
                                status = response.error.status ?: "ERROR",
                                message = response.error.message ?: "코스 전체 조회에 실패했습니다"
                            )
                        )
                    )

                    is ApiResponse.Success -> {
                        emit(ResponseStatus.Success(response.data.toDomainModel()))
                    }
                }
            } catch (e: Exception) {
                Timber.e("${e.message}")
                emit(
                    ResponseStatus.Error(
                        NetworkError(
                            message = e.message ?: "",
                        )
                    )
                )
            }
        }
    }
}