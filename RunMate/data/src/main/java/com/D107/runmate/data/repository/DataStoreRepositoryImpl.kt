package com.D107.runmate.data.repository

import com.D107.runmate.data.local.UserDataStoreSource
import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: UserDataStoreSource,
): DataStoreRepository {
    override val nickname: Flow<String?> = dataStore.nickname

    override suspend fun saveNickname(nickname: String) = dataStore.saveNickname(nickname)

    override val userId: Flow<String?> = dataStore.userId

    override suspend fun saveUserId(userId: String) = dataStore.saveUserId(userId)

    override val profileImage: Flow<String?> = dataStore.profileImage
    override suspend fun saveProfileImage(profileImage: String?) = dataStore.saveProfileImage(profileImage)

    override val accessToken: Flow<String?> = dataStore.accessToken

    override suspend fun saveAccessToken(accessToken: String) = dataStore.saveAccessToken(accessToken)

    override suspend fun clearAll() = dataStore.clearAll()

    override val savedInsoleAddresses: Flow<Pair<String?, String?>> = dataStore.savedAddressesFlow

    override suspend fun saveInsoleAddresses(leftAddress: String, rightAddress: String) = dataStore.saveInsoleAddresses(leftAddress, rightAddress)

    override suspend fun clearInsoleAddresses() = dataStore.clearInsoleAddresses()

    override val savedGaitAnalysisResult: Flow<GaitAnalysisResult?> = dataStore.savedGaitAnalysisResult

    override suspend fun saveGaitAnalysisResult(gaitAnalysisResult: GaitAnalysisResult) = dataStore.saveGaitAnalysisResult(gaitAnalysisResult)
}