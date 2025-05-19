package com.D107.runmate.domain.model.user

import com.D107.runmate.domain.model.base.BaseModel

data class UserInfo(
    val userId: String,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val birthday: String,
    val gender: String,
    val weight: Double,
    val height: Double
): BaseModel