package com.D107.runmate.watch.presentation.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.D107.runmate.watch.presentation.worker.GpxUploadWorker
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DataLayerListenerService : WearableListenerService() {
    // TAG 추가 (로깅용)
    private val TAG = "DataLayerListenerService"
    private val TEST_MESSAGE_PATH = "/test_message"




    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val messageClient by lazy { Wearable.getMessageClient(this) }

    companion object {

        // 러닝 상태 명령 path
        const val RUNNING_STATE_PATH = "/run_state"

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
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate: DataLayerListenerService 시작")

        testMessageReceiving()
    }

    private fun testMessageReceiving() {
        scope.launch {
            try {
                val messageClient = Wearable.getMessageClient(this@DataLayerListenerService)
                Log.d(TAG, "메시지 수신 리스너 등록 시도")

                // 메시지를 직접 받기 위한 리스너 등록
                messageClient.addListener { messageEvent ->
                    Log.d(
                        TAG,
                        "메시지 직접 수신: 경로=${messageEvent.path}, 소스=${messageEvent.sourceNodeId}"
                    )

//                    if (messageEvent.path == RUNNING_STATE_PATH) {
//                        val runstate = messageEvent.data
//                        Log.d(TAG, "직접 수신한 토큰: ${runstate.take(10)}...")
//                        saveRunStateToPrefs(runstate)
//                    }
                }

                Log.d(TAG, "메시지 수신 리스너 등록 완료")
            } catch (e: Exception) {
                Log.e(TAG, "메시지 수신 리스너 등록 실패: ${e.message}", e)
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: onDataChanged 호출됨")

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                val path = uri.path
                Log.d(TAG, "데이터 이벤트 경로: $path")

                if (path == RUNNING_STATE_PATH) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val runState = dataMap.getString("runstate", "")

                    // 토큰 수신 로그 (좀 더 명확하게)
                    Log.d(TAG, "워치에서 러닝 상태 수신 완료: ${runState}...")
                }
            }
        }
    }


//    private fun saveRunStateToPrefs(token: String) {
//        // SharedPreferences에 runstate 저장 예제
//        getSharedPreferences("run_state_prefs", MODE_PRIVATE)
//            .edit()
//            .putString("run_state", token)
//            .apply()
//
//        Log.d(TAG, "saveRunStateToPrefs: 러닝 상태가 워치 SharedPreferences에 저장됨")
//    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path

        Log.d(TAG, "💬 onMessageReceived: path=${messageEvent.path}, 데이터 크기=${messageEvent.data.size} 바이트")

        when (path) {
            RUNNING_STATE_PATH -> {
                // 러닝 상태 명령 처리
                if (messageEvent.data.isNotEmpty()) {
                    val state = messageEvent.data[0].toInt()
                    Log.d(TAG, "Received running state: $state")

                    // 상태 업데이트
                    _runningState.value = state

                    // 상태에 따른 화면 전환 처리
                    handleRunningState(state)
                }
            }
            TEST_MESSAGE_PATH -> {
                try {
                    val messageText = String(messageEvent.data, Charsets.UTF_8)
                    Log.d(TAG, "수신된 메시지 내용: $messageText")

                    // 메인 스레드에서 토스트 표시 (UI 업데이트)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(applicationContext, "메시지 수신: $messageText", Toast.LENGTH_LONG).show()
                    }

                    // 브로드캐스트 발송
                    val intent = Intent("com.D107.runmate.watch.TEST_MESSAGE")
                    intent.putExtra("message", messageText)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                    // 응답 전송
                    sendMessageAcknowledgment(messageEvent.sourceNodeId, messageText)
                } catch (e: Exception) {
                    Log.e(TAG, "메시지 처리 중 오류: ${e.message}", e)
                }
            }
        }
    }



    private fun sendMessageAcknowledgment(nodeId: String, originalMessage: String) {
        val response = "Received: $originalMessage"
        messageClient.sendMessage(nodeId, "/test_message_ack", response.toByteArray(Charsets.UTF_8))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully sent acknowledgment")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send acknowledgment: ${e.message}")
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

    // 러닝 상태 처리 함수
    private fun handleRunningState(state: Int) {
        val intent = Intent()

        when (state) {
            STATE_IDLE -> {
                // 폰과 워치가 연결된 상태 - 대기 중인 GPX 파일 확인
                Log.d(TAG, "STATE_IDLE: 폰과 연결됨. 대기 중인 GPX 파일 확인")
                checkAndTransferPendingGpxFiles()

                // MenuScreen에 버튼 비활성화 브로드캐스트 발송
                intent.action = "com.D107.runmate.watch.ACTION_DISABLE_BUTTONS"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            STATE_RUNNING -> {
                // 러닝 시작 - RunningScreen으로 전환
                Log.d(TAG, "STATE_RUNNING: 러닝 화면으로 전환")
                intent.action = ACTION_START_RUNNING
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                // 심박수 전송 시작
                startHeartRateTransmission()
            }
            STATE_PAUSED -> {
                // 러닝 일시정지 - PauseScreen으로 전환
                Log.d(TAG, "STATE_PAUSED: 일시정지 화면으로 전환")
                intent.action = ACTION_PAUSE_RUNNING
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                // 심박수 전송 일시 중지
                pauseHeartRateTransmission()
            }
            STATE_FINISHED -> {
                // 러닝 종료 - ResultScreen으로 전환
                Log.d(TAG, "STATE_FINISHED: 결과 화면으로 전환")
                intent.action = ACTION_FINISH_RUNNING
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                // 심박수 전송 중지
                stopHeartRateTransmission()

                // 3초 후 메뉴 화면으로 자동 전환을 위한 지연 처리
                Handler(Looper.getMainLooper()).postDelayed({
                    val menuIntent = Intent("com.D107.runmate.watch.ACTION_RETURN_TO_MENU")
                    LocalBroadcastManager.getInstance(this).sendBroadcast(menuIntent)
                }, 3000) // 3초 지연
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