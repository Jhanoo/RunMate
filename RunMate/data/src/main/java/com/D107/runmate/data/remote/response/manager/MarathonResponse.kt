package com.D107.runmate.data.remote.response.manager

import com.D107.runmate.data.remote.common.BaseResponse

data class MarathonResponse(
    val marathonId: String,
    val name: String,
    val date: String,
    val location: String,
    val distance: List<String>
) : BaseResponse