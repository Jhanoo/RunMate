package com.D107.runmate.watch.presentation.service

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "WearableService"

    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val dataClient = Wearable.getDataClient(context)

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 연결 상태
    private val _connectedToPhone = MutableStateFlow(false)
    val connectedToPhone: StateFlow<Boolean> = _connectedToPhone.asStateFlow()

    // 연결된 노드 정보
    private var connectedNode: Node? = null

    // JWT 토큰 상태
    private val _jwtToken = MutableStateFlow<String?>(null)
    val jwtToken: StateFlow<String?> = _jwtToken.asStateFlow()

    // 초기화
    init {
        setupDataListener()

        // 연결 상태 확인 시작
        checkConnection()
        startConnectionChecker()
    }

    private fun setupDataListener() {
        val dataListener = DataClient.OnDataChangedListener { dataEvents ->
            for (dataEvent in dataEvents) {
                if (dataEvent.type == DataEvent.TYPE_CHANGED) {
                    val dataItem = dataEvent.dataItem
                    Log.d(TAG, "데이터 변경 이벤트 수신: ${dataItem.uri.path}")

                    if (dataItem.uri.path == "/jwt_token") {
                        DataMapItem.fromDataItem(dataItem).dataMap.apply {
                            val token = getString("jwt")
                            if (!token.isNullOrEmpty()) {
                                // 토큰의 일부만 로그로 출력 (보안)
                                val tokenPreview = if (token.length > 20)
                                    "${token.substring(0, 10)}...${token.substring(token.length - 5)}"
                                else
                                    "짧은 토큰"

                                Log.d(TAG, "JWT 토큰 수신됨: $tokenPreview (전체 길이: ${token.length})")
                                serviceScope.launch {
                                    saveJwtToken(token)
                                    _jwtToken.value = token
                                    Log.d(TAG, "JWT 토큰 StateFlow 업데이트 완료")
                                }
                            } else {
                                Log.w(TAG, "수신된 JWT 토큰이 비어있거나 null입니다.")
                            }
                        }
                    }
                } else if (dataEvent.type == DataEvent.TYPE_DELETED) {
                    Log.d(TAG, "데이터 삭제 이벤트 수신: ${dataEvent.dataItem.uri.path}")
                }
            }
        }

        // 리스너 등록
        dataClient.addListener(dataListener)
        Log.d(TAG, "데이터 리스너 등록 완료")

        // 이후에 리스너를 제거할 때 사용하기 위해 변수에 저장
        this.dataListener = dataListener
    }

    // 클래스 멤버 변수로 리스너 참조 유지
    private var dataListener: DataClient.OnDataChangedListener? = null

    private suspend fun saveJwtToken(token: String) {
        try {
            context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).edit()
                .putString("jwt_token", token)
                .apply()

            // 저장 확인을 위한 즉시 읽기
            val savedToken = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("jwt_token", null)

            if (savedToken != null) {
                val tokenPreview = if (savedToken.length > 20)
                    "${savedToken.substring(0, 10)}...${savedToken.substring(savedToken.length - 5)}"
                else
                    "짧은 토큰"

                Log.d(TAG, "JWT 토큰 저장 성공: $tokenPreview (전체 길이: ${savedToken.length})")
            } else {
                Log.e(TAG, "JWT 토큰 저장 실패: 저장 후 읽기 시도했지만 null 값 반환됨")
            }
        } catch (e: Exception) {
            Log.e(TAG, "JWT 토큰 저장 중 오류 발생: ${e.message}", e)
        }
    }

    // 토큰 가져오기 기능
    suspend fun getJwtToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("jwt_token", null)

                if (token != null) {
                    val tokenPreview = if (token.length > 20)
                        "${token.substring(0, 10)}...${token.substring(token.length - 5)}"
                    else
                        "짧은 토큰"

                    Log.d(TAG, "저장된 JWT 토큰 검색 성공: $tokenPreview (전체 길이: ${token.length})")

                    // 저장된 토큰이 있으면 상태 업데이트
                    if (_jwtToken.value != token) {
                        _jwtToken.value = token
                        Log.d(TAG, "JWT 토큰 StateFlow 업데이트 완료 (저장된 토큰에서)")
                    }
                } else {
                    Log.d(TAG, "저장된 JWT 토큰 없음")
                }

                token
            } catch (e: Exception) {
                Log.e(TAG, "JWT 토큰 검색 중 오류 발생: ${e.message}", e)
                null
            }
        }
    }



    fun startConnectionChecker() {
        serviceScope.launch {
            while(true) {
                try {
                    findPhoneNode()
                    Log.d(TAG, "연결 상태 확인: ${if(_connectedToPhone.value) "연결됨" else "연결 안됨"}")
                } catch (e: Exception) {
                    Log.e(TAG, "연결 상태 확인 중 오류: ${e.message}")
                }
                delay(10000) // 10초마다 확인
            }
        }
    }

    // 연결 확인
    fun checkConnection() {
        serviceScope.launch {
            try {
                findPhoneNode()
            } catch (e: Exception) {
                Log.e(TAG, "연결 확인 실패: ${e.message}")
            }
        }
    }

    // 폰 노드 찾기
    private suspend fun findPhoneNode(): Node? {
        try {
            // 로컬 노드 ID 가져오기
            val localNodeTask = Tasks.await(nodeClient.localNode)
            val localNodeId = localNodeTask.id
            Log.d(TAG, "로컬 노드 ID: $localNodeId")

            // 모든 노드 가져오기 (연결 여부 상관없이)
            val nodes = Tasks.await(nodeClient.connectedNodes)
            Log.d(TAG, "연결된 노드 수: ${nodes.size}")

            if (nodes.isEmpty()) {
                // 연결 시도 다시 하기
                // 만약 BLE가 켜져 있는지 확인하고, 켜져 있다면 다시 시도
                Log.d(TAG, "연결된 노드가 없음. 연결 다시 시도...")
                _connectedToPhone.value = false
                return null
            }

            // 모든 노드 정보 로깅
            for (node in nodes) {
                Log.d(TAG, "노드 정보: ID=${node.id}, 이름=${node.displayName}, " +
                        "근처=${node.isNearby}, 로컬노드=${node.id == localNodeId}")

                // 로컬 노드가 아닌 모든 노드를 폰으로 간주
                if (node.id != localNodeId) {
                    connectedNode = node
                    _connectedToPhone.value = true
                    Log.d(TAG, "폰으로 간주되는 노드 찾음: ${node.displayName}")
                    return node
                }
            }

            Log.d(TAG, "폰 노드를 찾을 수 없음")
            _connectedToPhone.value = false
            return null
        } catch (e: Exception) {
            Log.e(TAG, "노드 검색 실패: ${e.message}", e)
            _connectedToPhone.value = false
            return null
        }
    }

    // 메시지 전송
    suspend fun sendMessage(path: String, data: ByteArray): Boolean {
        return try {
            val node = connectedNode ?: findPhoneNode() ?: return false

            Tasks.await(messageClient.sendMessage(node.id, path, data))
            Log.d(TAG, "메시지 전송 성공: $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "메시지 전송 실패: $path, ${e.message}")
            false
        }
    }

    // 심박수 데이터 전송
    suspend fun sendHeartRate(heartRate: Int): Boolean {
        return sendMessage("/heart_rate", byteArrayOf(heartRate.toByte()))
    }

    // 러닝 데이터 전송
    suspend fun sendRunningData(heartRate: Int, pace: String, distance: Double, cadence: Int): Boolean {
        val data = """{"hr":$heartRate,"pace":"$pace","distance":$distance,"cadence":$cadence}"""
        return sendMessage("/running_data", data.toByteArray())
    }

    // 리소스 해제
    fun cleanup() {
        serviceScope.cancel()
        dataListener?.let { listener ->
            dataClient.removeListener(listener)
        }
    }

    fun checkActiveDataItems() {
        serviceScope.launch {
            try {
                val dataItems = Tasks.await(dataClient.dataItems)
                Log.d(TAG, "현재 활성 데이터 항목 수: ${dataItems.count}")

                dataItems.forEach { item ->
                    Log.d(TAG, "데이터 항목 경로: ${item.uri.path}")

                    if (item.uri.path == "/jwt_token") {
                        val dataMap = DataMapItem.fromDataItem(item).dataMap
                        val token = dataMap.getString("jwt")
                        if (!token.isNullOrEmpty()) {
                            val tokenPreview = if (token.length > 20)
                                "${token.substring(0, 10)}...${token.substring(token.length - 5)}"
                            else
                                "짧은 토큰"

                            Log.d(TAG, "활성 JWT 토큰 찾음: $tokenPreview (길이: ${token.length})")

                            // 토큰 저장 및 상태 업데이트
                            serviceScope.launch {
                                saveJwtToken(token)
                                _jwtToken.value = token
                            }
                        }
                    }
                }

                dataItems.release()
            } catch (e: Exception) {
                Log.e(TAG, "데이터 항목 확인 중 오류: ${e.message}")
            }
        }
    }
}