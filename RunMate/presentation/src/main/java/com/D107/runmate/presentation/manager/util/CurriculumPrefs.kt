package com.D107.runmate.presentation.manager.util

import android.content.Context
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurriculumPrefs {
    private const val PREF_NAME = "curriculum_prefs"
    private const val KEY_LAST_REFRESH = "last_refresh_time"

    fun saveRefreshTime(context: Context) {
        val currentTime = System.currentTimeMillis()
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putLong(KEY_LAST_REFRESH, currentTime)
            .apply()

        // 저장된 시간을 가독성 있는 형태로 로그 출력
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(currentTime))
        Timber.d("커리큘럼 새로고침 시간이 저장되었습니다: $formattedDate ($currentTime)")
    }

//    fun canRefresh(context: Context): Boolean {
//        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val lastRefreshTime = prefs.getLong(KEY_LAST_REFRESH, 0L)
//
//        // 저장된 값이 없으면 true 반환 (첫 실행 시 항상 가능)
//        if (lastRefreshTime == 0L) {
//            Timber.d("커리큘럼 새로고침 시간이 아직 저장되지 않았습니다. 새로고침 가능.")
//            return true
//        }
//
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//        val formattedDate = dateFormat.format(Date(lastRefreshTime))
//
//        val currentTime = System.currentTimeMillis()
//        val diffDays = TimeUnit.MILLISECONDS.toDays(currentTime - lastRefreshTime)
//
//        val canRefresh = diffDays >= 7
//        Timber.d("마지막 새로고침 시간: $formattedDate, 경과 시간: ${diffDays}일, 새로고침 가능 여부: $canRefresh")
//
//        return canRefresh // 7일 후에 refresh 가능하도록 설정
//    }

    fun canRefresh(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastRefreshTime = prefs.getLong(KEY_LAST_REFRESH, 0L)

        // 저장된 값이 없으면 true 반환 (첫 실행 시 항상 가능)
        if (lastRefreshTime == 0L) {
            return true
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(lastRefreshTime))

        val currentTime = System.currentTimeMillis()
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastRefreshTime)

        val canRefresh = diffMinutes >= 1

        return canRefresh // 1분 후에 refresh 가능하도록 설정
    }

    // SharedPreferences에 저장된 모든 값 출력 (디버깅용)
    fun logAllPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastRefreshTime = prefs.getLong(KEY_LAST_REFRESH, 0L)

        if (lastRefreshTime == 0L) {
            Timber.d("SharedPreferences($PREF_NAME): 저장된 새로고침 시간이 없습니다.")
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(lastRefreshTime))
            Timber.d("SharedPreferences($PREF_NAME): 마지막 새로고침 시간 = $formattedDate ($lastRefreshTime)")
        }
    }
}