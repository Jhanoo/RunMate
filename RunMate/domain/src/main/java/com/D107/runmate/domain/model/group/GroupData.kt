package com.D107.runmate.domain.model.group

import com.D107.runmate.domain.model.base.BaseModel
import java.time.OffsetDateTime

data class GroupData(

    val groupId: String = "",

    val groupName: String = "",

    val leaderId: String = "",

    val courseId: String = "",

    val startTime: String = "",

    val startLocation: String = "",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val inviteCode: String = "",

    val members: List<GroupMemberData> = emptyList()
):BaseModel