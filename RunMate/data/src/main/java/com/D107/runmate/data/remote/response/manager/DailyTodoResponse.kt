package com.D107.runmate.data.remote.response.manager

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.manager.TodoItem
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DailyTodoResponse(
    val content: String,
    val date: String,
    val isDone: Boolean,
    val todoId: String
): BaseResponse {
    companion object: DataMapper<DailyTodoResponse, TodoItem> {
        override fun DailyTodoResponse.toDomainModel(): TodoItem {
            return TodoItem(todoId, content, isDone, date)
        }
    }
}