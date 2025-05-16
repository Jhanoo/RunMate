package com.D107.runmate.domain.model.history

import com.D107.runmate.domain.model.base.BaseModel

data class HistoryDetail(
    val gpxFile: String,
    val groupRunItem: List<GroupRun?>,
    val historyId: String,
    val myRunItem: MyRun
): BaseModel
