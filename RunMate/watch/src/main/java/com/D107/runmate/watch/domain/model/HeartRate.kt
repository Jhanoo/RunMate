package com.D107.runmate.watch.domain.model

data class HeartRate (
    val bpm: Int,
    val timestamp: Long = System.currentTimeMillis()
)