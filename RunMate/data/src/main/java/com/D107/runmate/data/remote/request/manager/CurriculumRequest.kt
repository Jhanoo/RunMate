package com.D107.runmate.data.remote.request.manager

data class CurriculumRequest(
    val marathonId: String,
    val goalDist: String,
    val goalDate: String,
    val runExp: Boolean,
    val distExp: String,
    val freqExp: String
)