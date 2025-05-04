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

    val savedInsoleAddresses: Flow<Pair<String?, String?>> // 이름 변경 고려 (savedAddressesFlow -> savedInsoleAddresses)
    suspend fun saveInsoleAddresses(leftAddress: String, rightAddress: String)
    suspend fun clearInsoleAddresses()
}