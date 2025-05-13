package com.D107.runmate.domain.model.manager

data class TodoItem(
    val todoId: String,
    val content: String,
    val isDone: Boolean,
    val date: String
)