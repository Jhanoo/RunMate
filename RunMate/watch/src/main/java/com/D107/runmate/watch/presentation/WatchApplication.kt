package com.D107.runmate.watch.presentation

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.D107.runmate.watch.presentation.service.BluetoothService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WatchApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var bluetoothService: BluetoothService

    //    companion object {
//        private lateinit var instance: WatchApplication
//
//        fun getBluetoothService(): BluetoothService {
//            return instance.bluetoothService
//        }
//    }
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
//        GpxUploadWorker.schedulePeriodic(this)
//        instance = this
    }



}