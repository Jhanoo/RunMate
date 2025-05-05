package com.D107.runmate.watch.domain.model

data class Distance(
    val kilometers: Double,
    val timestamp: Long = System.currentTimeMillis()
)