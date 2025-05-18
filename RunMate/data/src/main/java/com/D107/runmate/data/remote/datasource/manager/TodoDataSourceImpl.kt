package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.api.TodoService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.response.manager.DailyTodoResponse
import com.D107.runmate.data.remote.response.manager.TodoResponse
import javax.inject.Inject

class TodoDataSourceImpl @Inject constructor(
    private val todoService: TodoService
) : TodoDataSource {
    override suspend fun getTodoList(year: Int, month: Int): ApiResponse<List<TodoResponse>> {
        return try {
            todoService.getTodoList(year, month)
        } catch (e: Exception) {
            ApiResponse.Error(
                ErrorResponse(
                    status = "NETWORK_ERROR",
                    error = "CONNECTION_FAILED",
                    code = "NETWORK_ERROR",
                    message = "서버에 연결할 수 없습니다: ${e.message}"
                )
            )
        }
    }

    override suspend fun getTodayTodo(): ApiResponse<DailyTodoResponse> {
        return todoService.getTodayTodo()
    }
}