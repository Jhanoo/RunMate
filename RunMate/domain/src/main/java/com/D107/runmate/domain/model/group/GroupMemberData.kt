package com.D107.runmate.domain.model.group

import com.D107.runmate.domain.model.base.BaseModel


data class GroupMemberData(
    val memberId: String = "",
    val nickname: String = "",
    val profileImage: String = ""
):BaseModel