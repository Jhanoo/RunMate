package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.response.manager.DailyTodoResponse
import com.D107.runmate.data.remote.response.manager.TodoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TodoService {
    @GET("curricula/todoList")
    suspend fun getTodoList(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ApiResponse<List<TodoResponse>>

    @GET("curricula/today")
    suspend fun getTodayTodo(): ApiResponse<DailyTodoResponse>
}