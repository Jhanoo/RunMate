package com.D107.runmate.domain.model.running

import com.D107.runmate.domain.model.base.BaseModel

data class Creator(
    val nickname: String,
    val profileImage: String
): BaseModel
