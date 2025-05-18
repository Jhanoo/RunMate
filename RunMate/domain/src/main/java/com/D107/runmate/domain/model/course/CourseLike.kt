package com.D107.runmate.domain.model.course

import com.D107.runmate.domain.model.base.BaseModel

data class CourseLike(
    val liked: Boolean,
    val totalLikes: Int
): BaseModel
