package com.D107.runmate.domain.model.user

import com.D107.runmate.domain.model.base.BaseModel

data class UserInfo(
    val userId: Int,
    val nickname: String,
    val birthYear: Int,
    val userJob: String,
    val email: String? = null,
    val profileImage: String? = null,
    val gender: String? = null
): BaseModel