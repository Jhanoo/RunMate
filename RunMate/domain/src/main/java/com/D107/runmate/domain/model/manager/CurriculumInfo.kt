package com.D107.runmate.domain.model.manager

import com.D107.runmate.domain.model.base.BaseModel

data class CurriculumInfo(
    val curriculumId: String,
    val marathonId: String = "",
    val goalDist: String = "",
    val goalDate: String = "",
    val runExp: Boolean = false,
    val distExp: String = "",
    val freqExp: String = ""
) : BaseModel