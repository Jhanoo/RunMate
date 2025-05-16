package com.D107.runmate.data.repository

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.manager.TodoDataSource
import com.D107.runmate.domain.model.manager.TodoItem
import com.D107.runmate.domain.repository.manager.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
}