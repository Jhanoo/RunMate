package com.D107.runmate.domain.model.manager

import com.D107.runmate.domain.model.base.BaseModel

data class MarathonInfo(
    val id: String,
    val title: String,
    val date: String,
    val dayOfWeek: String,
    val location: String,
    val distance: List<String> = emptyList()
) : BaseModel