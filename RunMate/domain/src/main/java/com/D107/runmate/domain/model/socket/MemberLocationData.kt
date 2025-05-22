package com.D107.runmate.domain.model.socket

data class MemberLocationData(
    val userId: String,
    val nickname: String,
    val profileImage: String?,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)
