package com.D107.runmate.watch.presentation

import android.app.Application
import com.D107.runmate.watch.presentation.service.BluetoothService
import com.D107.runmate.watch.presentation.worker.GpxUploadWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WatchApplication : Application() {
    @Inject
    lateinit var bluetoothService: BluetoothService

    companion object {
        private lateinit var instance: WatchApplication

        fun getBluetoothService(): BluetoothService {
            return instance.bluetoothService
        }
    }
    override fun onCreate() {
        super.onCreate()
        GpxUploadWorker.schedulePeriodic(this)
        instance = this
    }
}