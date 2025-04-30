package com.D107.runmate.domain.model.user

import com.D107.runmate.domain.model.base.BaseModel

data class UserInfo(
    val birthYear: Int,
    val nickname: String,
    val userId: Int,
    val userJob: String
): BaseModel
