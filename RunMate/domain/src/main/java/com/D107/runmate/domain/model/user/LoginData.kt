package com.D107.runmate.domain.model.user

import com.D107.runmate.domain.model.base.BaseModel

data class LoginData(
    val accessToken: String
) : BaseModel