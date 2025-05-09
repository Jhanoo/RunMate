package com.D107.runmate.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_VIBRATE -> {
                Log.d(TAG, "onReceive: ACTION_VIBRATE")
            }

            ACTION_SOUND -> {
                Log.d(TAG, "onReceive: ACTION_SOUND")
            }

            ACTION_PAUSE -> {
                Log.d(TAG, "onReceive: ACTION_PAUSE")
            }

            ACTION_START -> {
                Log.d(TAG, "onReceive: ACTION_START")
            }

            ACTION_STOP -> {
                Log.d(TAG, "onReceive: ACTION_STOP")
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