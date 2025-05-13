package com.D107.runmate.data.remote.response.manager

import com.D107.runmate.domain.model.manager.TodoItem

data class TodoResponse(
    val message: String,
    val data: List<TodoItem>
)