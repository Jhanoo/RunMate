package com.D107.runmate.domain.usecase.manager

import com.D107.runmate.domain.repository.manager.TodoRepository
import javax.inject.Inject

class GetDailyTodoUseCase @Inject constructor(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke() = todoRepository.getDailyTodo()
}