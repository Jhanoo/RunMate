package com.D107.runmate.domain.model.manager

import com.D107.runmate.domain.model.base.BaseModel

data class TodoItem(
    val todoId: String,
    val content: String,
    val isDone: Boolean?,
    val date: String
): BaseModel