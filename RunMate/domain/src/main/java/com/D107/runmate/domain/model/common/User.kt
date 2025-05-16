package com.D107.runmate.domain.model.common

import com.D107.runmate.domain.model.base.BaseModel

data class User(
    val memberId: String = "",
    val userId: String = "",
    val nickname: String = "",
    val profileImage: String?
):BaseModel
