package com.D107.runmate.domain.model.smartinsole

data class CombinedInsoleData(
    val left: InsoleData?,
    val right: InsoleData?,
    val timestamp: Long = System.currentTimeMillis()
)