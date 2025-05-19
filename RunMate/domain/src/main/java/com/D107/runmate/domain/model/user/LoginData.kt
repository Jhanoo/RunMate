package com.D107.runmate.domain.model.user

import com.D107.runmate.domain.model.base.BaseModel

data class LoginData(
    val accessToken: String,
    val userId: String? = null,
    val nickname: String? = null,
    val profileImage: String? = null,
    val weight: Double? = null,
    val height: Double? = null
) : BaseModel