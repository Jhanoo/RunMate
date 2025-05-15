package com.D107.runmate.domain.model.manager

import com.D107.runmate.domain.model.base.BaseModel

data class CurriculumInfo(
    val curriculumId: String,
    val marathonId: String = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    val goalDist: String = "10km",
    val goalDate: String = "2025-06-10T09:00:00+09:00",
    val runExp: Boolean = true,
    val distExp: String = "~10km",
    val freqExp: String = "1~2회"
) : BaseModel