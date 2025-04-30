package com.D107.runmate.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val nickname: Flow<String?>
    suspend fun saveNickname(nickname: String)

    val userId: Flow<Long?>
    suspend fun saveUserId(userId: Long)

    val accessToken: Flow<String?>
    suspend fun saveAccessToken(accessToken: String)

    suspend fun clearAll()
}