package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.api.TodoService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.common.ErrorResponse
import com.D107.runmate.data.remote.response.manager.DailyTodoResponse
import com.D107.runmate.data.remote.response.manager.TodoResponse
import javax.inject.Inject

class TodoDataSourceImpl @Inject constructor(
    private val todoService: TodoService,
    private val apiResponseHandler: ApiResponseHandler
) : TodoDataSource {
    override suspend fun getTodoList(year: Int, month: Int): ApiResponse<List<TodoResponse>> {
        return apiResponseHandler.handle {
            todoService.getTodoList(year, month)
        }
    }

    override suspend fun getTodayTodo(): ApiResponse<DailyTodoResponse> {
        return apiResponseHandler.handle {
            todoService.getTodayTodo()
        }
    }
}