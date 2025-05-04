package com.D107.runmate.domain.model.Insole

data class SmartInsole(
    val name: String?,
    val address: String,
    val side: InsoleSide = InsoleSide.UNKNOWN
)
enum class InsoleSide { LEFT, RIGHT, UNKNOWN }