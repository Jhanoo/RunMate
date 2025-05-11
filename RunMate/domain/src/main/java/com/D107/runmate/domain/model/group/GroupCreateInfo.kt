package com.D107.runmate.domain.model.group

import java.time.OffsetDateTime

data class GroupCreateInfo(
    val groupName: String = "",
    val courseId: String? = null,
    val startTime: OffsetDateTime = OffsetDateTime.now(),
    val startLocation: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0

)