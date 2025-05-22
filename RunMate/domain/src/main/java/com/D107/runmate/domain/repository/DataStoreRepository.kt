package com.D107.runmate.domain.repository

import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val nickname: Flow<String?>
    suspend fun saveNickname(nickname: String)

    val userId: Flow<String?>
    suspend fun saveUserId(userId: String)

    val profileImage: Flow<String?>
    suspend fun saveProfileImage(profileImage: String?)

    val accessToken: Flow<String?>
    suspend fun saveAccessToken(accessToken: String)

    suspend fun clearAll()

    val savedInsoleAddresses: Flow<Pair<String?, String?>> // 이름 변경 고려 (savedAddressesFlow -> savedInsoleAddresses)
    suspend fun saveInsoleAddresses(leftAddress: String, rightAddress: String)
    suspend fun clearInsoleAddresses()

    val savedGaitAnalysisResult: Flow<GaitAnalysisResult?>
    suspend fun saveGaitAnalysisResult(gaitAnalysisResult: GaitAnalysisResult)

    val weight: Flow<Double?>
    suspend fun saveWeight(weight: Double)

    val height: Flow<Double?>
    suspend fun saveHeight(height: Double)

    val fcmToken: Flow<String?>
    suspend fun saveFcmToken(fcmToken: String)

}