package com.D107.runmate.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class UserDataStoreSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) {
    companion object {
        val NICKNAME = stringPreferencesKey("nickname")
        val USER_ID = longPreferencesKey("user_id")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val LEFT_INSOLE_ADDRESS = stringPreferencesKey("left_insole_address")
        val RIGHT_INSOLE_ADDRESS = stringPreferencesKey("right_insole_address")
        val GAIT_ANALYSIS_RESULT = stringPreferencesKey("gait_analysis_result")
    }

    suspend fun saveNickname(nickname: String) {
        dataStore.edit { preferences ->
            preferences[NICKNAME] = nickname
        }
    }

    suspend fun saveUserId(userId: Long) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    suspend fun saveAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun saveGaitAnalysisResult(result:GaitAnalysisResult){
        val jsonString = json.encodeToString(result)
        dataStore.edit { preferences ->
            preferences[GAIT_ANALYSIS_RESULT] = jsonString
        }

    }

    val nickname: Flow<String?> = dataStore.data.map { preferences ->
        preferences[NICKNAME]
    }

    val userId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val leftInsoleAddressFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[LEFT_INSOLE_ADDRESS]
        }

    val rightInsoleAddressFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[RIGHT_INSOLE_ADDRESS]
        }

    val savedAddressesFlow: Flow<Pair<String?, String?>> =
        leftInsoleAddressFlow.combine(rightInsoleAddressFlow) { left, right ->
            Pair(left, right)
        }

    val savedGaitAnalysisResult:Flow<GaitAnalysisResult?> = flow{
        val jsonString = dataStore.data
            .map { preferences ->
                preferences[GAIT_ANALYSIS_RESULT]
            }
            .first()

        if (jsonString.isNullOrBlank()) {
            Timber.d("No saved gait analysis found in DataStore.")
            emit(null)
        } else {
            emit(json.decodeFromString<GaitAnalysisResult>(jsonString))
        }
    }

    suspend fun saveInsoleAddresses(leftAddress: String, rightAddress: String) {
        dataStore.edit { preferences ->
            preferences[LEFT_INSOLE_ADDRESS] = leftAddress
            preferences[RIGHT_INSOLE_ADDRESS] = rightAddress
        }
    }

    suspend fun clearInsoleAddresses() {
        dataStore.edit { preferences ->
            preferences.remove(LEFT_INSOLE_ADDRESS)
            preferences.remove(RIGHT_INSOLE_ADDRESS)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}