package com.D107.runmate.data.remote.response.manager

import com.D107.runmate.data.remote.common.BaseResponse

data class CurriculumDetailResponse(
    val curriculumId: String,
    val marathonId: String? = null,
    val goalDist: String? = null,
    val goalDate: String? = null,
    val runExp: Boolean? = null,
    val distExp: String? = null,
    val freqExp: String? = null
) : BaseResponse