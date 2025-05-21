package com.D107.runmate.presentation.utils

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object WatchDataUtils {
    private const val TEST_MESSAGE_PATH = "/test_message"

//    suspend fun sendTokenToWatch(context: Context, token: String) {
//        withContext(Dispatchers.IO) {
//            val dataClient = Wearable.getDataClient(context)
//            val dataMapRequest = PutDataMapRequest.create("/jwt_token")
//            dataMapRequest.dataMap.putString("jwt", token)
//            val request = dataMapRequest.asPutDataRequest().setUrgent()
//            dataClient.putDataItem(request).addOnSuccessListener {
//                // 성공 로그 등
//                Timber.d("sendTokenToWatch success ${token}")
//            }
//                .addOnFailureListener {
//                    Timber.e("sendTokenToWatch Fail ${it.message}")
//                }
//        }
//    }

//    suspend fun sendTestMessage(context: Context) {
//        withContext(Dispatchers.IO) {
//            try {
//                // 연결된 노드 가져오기
//                val nodeClient = Wearable.getNodeClient(context)
//                val nodes = Tasks.await(nodeClient.connectedNodes)
//
//                if (nodes.isEmpty()) {
//                    Timber.w("연결된 워치 기기 없음")
//                    return@withContext
//                }
//
//                // 메시지 클라이언트
//                val messageClient = Wearable.getMessageClient(context)
//                val testMessage = "간단 테스트 ${System.currentTimeMillis()}"
//
//                for (node in nodes) {
//                    // 더 명확한 로그
//                    Timber.d("💬 테스트 메시지 전송 - 노드: ${node.displayName}, ID: ${node.id}")
//
//                    messageClient.sendMessage(node.id, TEST_MESSAGE_PATH, testMessage.toByteArray())
//                        .addOnSuccessListener {
//                            Timber.d("✅ 메시지 전송 성공 to ${node.displayName}")
//
//                            // 응답 리스너 설정
//                            messageClient.addListener { event ->
//                                if (event.path == "/test_message_ack") {
//                                    val response = String(event.data, Charsets.UTF_8)
//                                    Timber.d("📱 응답 수신: $response from ${event.sourceNodeId}")
//                                }
//                            }
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.e("❌ 메시지 전송 실패: ${e.message}")
//                        }
//                }
//            } catch (e: Exception) {
//                Timber.e("테스트 메시지 전송 중 오류: ${e.message}")
//            }
//        }
//    }


}