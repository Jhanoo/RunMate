package com.D107.runmate.data.remote.response.manager

data class TodoResponse(
    val todoId: String,
    val content: String,
    val isDone: Boolean?,
    val date: String
)