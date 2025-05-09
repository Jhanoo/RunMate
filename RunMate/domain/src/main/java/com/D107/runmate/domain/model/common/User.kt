package com.D107.runmate.domain.model.common

import com.D107.runmate.domain.model.base.BaseModel

data class User(
    val userId:String,
    val nickname:String,
    val userProfileImg:String?
):BaseModel
