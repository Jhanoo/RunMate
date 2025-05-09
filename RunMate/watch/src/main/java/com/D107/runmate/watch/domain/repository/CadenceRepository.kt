package com.D107.runmate.watch.domain.repository

interface CadenceRepository {
    fun getCurrentCadence(): Int
}