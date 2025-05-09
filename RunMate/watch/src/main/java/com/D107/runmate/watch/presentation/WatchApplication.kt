package com.D107.runmate.watch.presentation

import android.app.Application
import com.D107.runmate.watch.presentation.worker.GpxUploadWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GpxUploadWorker.schedulePeriodic(this)
    }
}