package com.D107.runmate.domain.repository.manager

import com.D107.runmate.domain.model.manager.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    suspend fun getTodoList(year: Int, month: Int): Flow<Result<List<TodoItem>>>
}