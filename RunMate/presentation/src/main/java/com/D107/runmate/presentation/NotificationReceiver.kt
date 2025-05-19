package com.D107.runmate.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: RunningTrackingRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_VIBRATE -> {
                Timber.d("onReceive: ACTION_VIBRATE")
            }

            ACTION_SOUND -> {
                Timber.d("onReceive: ACTION_SOUND")
            }

            ACTION_PAUSE -> {
                context?.let {
                    RunningTrackingService.pauseService(it)
                    repository.setTrackingStatus(TrackingStatus.PAUSED)
                }
            }

            ACTION_START -> {
                context?.let {
                    RunningTrackingService.startService(it)
                    repository.setTrackingStatus(TrackingStatus.RUNNING)
                }
            }

            ACTION_STOP -> {
                context?.let {
                    RunningTrackingService.stopService(it)
                    repository.setTrackingStatus(TrackingStatus.STOPPED)
                }
            }
        }
    }

    companion object {
        const val ACTION_VIBRATE = "ACTION_VIBRATE"
        const val ACTION_SOUND = "ACTION_SOUND"
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
    }
}