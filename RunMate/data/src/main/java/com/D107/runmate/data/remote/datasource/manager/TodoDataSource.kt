package com.D107.runmate.data.remote.datasource.manager

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.response.manager.TodoResponse

interface TodoDataSource {
    suspend fun getTodoList(year: Int, month: Int): ApiResponse<List<TodoResponse>>
}