package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class HistoryInfo(
    val histories: List<History>,
    val page: Int,
    val size: Int,
    val total: Int
): BaseModel
