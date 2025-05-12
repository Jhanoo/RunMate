package com.D107.runmate.domain.model.running

import java.util.Date

data class GpxMetadata(
    val name: String,
    val desc: String = "",
    val time: String,
)