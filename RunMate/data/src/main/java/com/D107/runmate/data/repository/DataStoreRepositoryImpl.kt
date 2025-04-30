package com.D107.runmate.data.repository

import com.D107.runmate.data.local.UserDataStoreSource
import com.D107.runmate.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: UserDataStoreSource,
): DataStoreRepository {
    override val nickname: Flow<String?> = dataStore.nickname

    override suspend fun saveNickname(nickname: String) = dataStore.saveNickname(nickname)

    override val userId: Flow<Long?> = dataStore.userId

    override suspend fun saveUserId(userId: Long) = dataStore.saveUserId(userId)

    override val accessToken: Flow<String?> = dataStore.accessToken

    override suspend fun saveAccessToken(accessToken: String) = dataStore.saveAccessToken(accessToken)

    override suspend fun clearAll() = dataStore.clearAll()
}