package com.D107.runmate

import android.app.Application
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

private const val TAG = "App"
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, BuildConfig.NATIVE_API_KEY)
        Timber.plant(Timber.DebugTree())
    }
}