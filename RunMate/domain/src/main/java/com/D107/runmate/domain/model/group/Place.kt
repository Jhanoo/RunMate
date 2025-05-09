package com.D107.runmate.domain.model.group

import com.D107.runmate.domain.model.base.BaseModel

data class Place(
    val id:String,
    val name: String,
    val address: String,
    val x: String,
    val y: String
):BaseModel
