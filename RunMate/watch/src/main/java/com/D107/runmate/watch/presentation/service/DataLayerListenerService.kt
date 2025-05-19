// DataLayerListenerService.kt
package com.D107.runmate.watch.presentation.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.D107.runmate.watch.presentation.worker.GpxUploadWorker
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {

    companion object {
        // 러닝 상태 명령 path
        const val RUNNING_STATE_PATH = "/running_state"

        // 러닝 상태 값
        const val STATE_IDLE = 0     // 시작 전
        const val STATE_RUNNING = 1  // 러닝중
        const val STATE_PAUSED = 2   // 정지
        const val STATE_FINISHED = 3 // 종료

        // 현재 러닝 상태를 저장할 StateFlow
        private val _runningState = MutableStateFlow(STATE_IDLE)
        val runningState: StateFlow<Int> = _runningState

        // ACTION 상수 추가
        const val ACTION_START_RUNNING = "com.D107.runmate.watch.ACTION_START_RUNNING"
        const val ACTION_PAUSE_RUNNING = "com.D107.runmate.watch.ACTION_PAUSE_RUNNING"
        const val ACTION_RESUME_RUNNING = "com.D107.runmate.watch.ACTION_RESUME_RUNNING"
        const val ACTION_FINISH_RUNNING = "com.D107.runmate.watch.ACTION_FINISH_RUNNING"

        // TAG 추가 (로깅용)
        private const val TAG = "DataLayerListenerService"

        // 기존 코드에 있을 것으로 가정한 상수
        const val SYNC_REQUEST_PATH = "/sync_request"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        Log.d(TAG, "Message received: $path")

        when {
            path.startsWith(SYNC_REQUEST_PATH) -> {
                Log.d(TAG, "폰과 연결됨. 대기 중인 GPX 파일 확인")
                checkAndTransferPendingGpxFiles()
            }
            path.startsWith(RUNNING_STATE_PATH) -> {
                // 러닝 상태 명령 처리
                val data = messageEvent.data
                if (data.isNotEmpty()) {
                    val state = data[0].toInt()
                    Log.d(TAG, "Received running state: $state")

                    // 상태 업데이트
                    _runningState.value = state

                    // 상태에 따른 화면 전환 처리
                    handleRunningState(state)
                }
            }
        }
    }

    private fun checkAndTransferPendingGpxFiles() {
        // 폰과 연결되었을 때 대기 중인 GPX 파일 전송
        WorkManager.getInstance(this).enqueueUniqueWork(
            "transfer_gpx_files",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<GpxUploadWorker>().build()
        )
    }

    // 러닝 상태 처리 함수 추가
    private fun handleRunningState(state: Int) {
        val intent = Intent()

        when (state) {
            STATE_RUNNING -> {
                // 러닝 시작 - RunningScreen으로 전환
                intent.action = ACTION_START_RUNNING
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                // 심박수 전송 시작
                startHeartRateTransmission()
            }
            STATE_PAUSED -> {
                // 러닝 일시정지 - PauseScreen으로 전환
                intent.action = ACTION_PAUSE_RUNNING
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                // 심박수 전송 일시 중지
                pauseHeartRateTransmission()
            }
            STATE_FINISHED -> {
                // 러닝 종료 - ResultScreen으로 전환
                intent.action = ACTION_FINISH_RUNNING
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                // 심박수 전송 중지
                stopHeartRateTransmission()
            }
        }
    }

    // 심박수 전송 관련 함수 추가
    private fun startHeartRateTransmission() {
        // 심박수 전송 시작 로직
        val startHeartRateIntent = Intent(this, LocationTrackingService::class.java)
        startHeartRateIntent.action = LocationTrackingService.ACTION_START
        startService(startHeartRateIntent)
    }

    private fun pauseHeartRateTransmission() {
        // 심박수 전송 일시 중지 로직
        val pauseHeartRateIntent = Intent(this, LocationTrackingService::class.java)
        pauseHeartRateIntent.action = LocationTrackingService.ACTION_PAUSE
        startService(pauseHeartRateIntent)
    }

    private fun stopHeartRateTransmission() {
        // 심박수 전송 중지 로직
        val stopHeartRateIntent = Intent(this, LocationTrackingService::class.java)
        stopHeartRateIntent.action = LocationTrackingService.ACTION_STOP
        startService(stopHeartRateIntent)
    }
}