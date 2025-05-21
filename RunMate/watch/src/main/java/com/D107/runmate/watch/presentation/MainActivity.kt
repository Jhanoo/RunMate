package com.D107.runmate.watch.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Half.toFloat
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.D107.runmate.watch.presentation.menu.MenuScreen
import com.D107.runmate.watch.presentation.pace.PaceScreen
import com.D107.runmate.watch.presentation.running.DisplayMode
import com.D107.runmate.watch.presentation.running.PauseScreen
import com.D107.runmate.watch.presentation.running.ResultScreen
import com.D107.runmate.watch.presentation.running.RunningData
import com.D107.runmate.watch.presentation.running.RunningScreen
import com.D107.runmate.watch.presentation.running.RunningViewModel
import com.D107.runmate.watch.presentation.service.BluetoothService
import com.D107.runmate.watch.presentation.service.DataLayerListenerService
import com.D107.runmate.watch.presentation.splash.SplashScreen
import com.D107.runmate.watch.presentation.theme.RunMateTheme
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutionException

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var runningViewModel: RunningViewModel

    private var navController: NavController? = null
    private var menuButtonsEnabled = true

    private val CONNECTION_CHECK_INTERVAL = 15000L // 15초마다 확인
    private var connectionCheckHandler: Handler? = null
    private val connectionCheckRunnable = object : Runnable {
        override fun run() {
            checkPhoneConnectionAndUpdateUI()
            connectionCheckHandler?.postDelayed(this, CONNECTION_CHECK_INTERVAL)
        }
    }

//    private fun startHeartRateOnly() {
//        lifecycleScope.launch {
//            Log.d("HeartRate", "심박수 전송 시작")
//            runningViewModel.startHeartRateOnlyTracking(applicationContext)
//        }
//    }

    @SuppressLint("StateFlowValueCalledInComposition", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setupMessageReceivers()

        // 초기 연결 상태 확인
        checkPhoneConnectionAndUpdateUI()

        // 주기적 연결 상태 확인
        startConnectionMonitoring()

        val serviceIntent = Intent(this, DataLayerListenerService::class.java)
        startService(serviceIntent)
        Log.d("MainActivity", "DataLayerListenerService 명시적 시작")

        splashScreen.setKeepOnScreenCondition { false }

        setTheme(android.R.style.Theme_DeviceDefault)

        // 권환 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ),
                BODY_SENSOR_PERMISSION_REQUEST_CODE
            )
        }

        setContent {
            RunMateTheme {
                val navController = rememberSwipeDismissableNavController()

                val runningViewModel: RunningViewModel = hiltViewModel()
                // RunningScreen의 상태를 저장할 변수들
                var savedTopIndex by remember { mutableStateOf(0) }
                var savedLeftIndex by remember { mutableStateOf(1) }
                var savedRightIndex by remember { mutableStateOf(2) }
                var savedPace by remember { mutableStateOf("0:00") }
                var savedRunningData by remember { mutableStateOf(RunningData()) }

                this@MainActivity.runningViewModel = runningViewModel

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {

                    // 스플래시 화면에서 메뉴 화면으로 이동 (백스택x)
                    composable("splash") {
                        SplashScreen(
                            onTimeout = {
                                navController.navigate("menu") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 메뉴 화면에서 러닝, 페이스 설정 화면으로 이동
                    composable("menu") {
                        MenuScreen(
                            onNavigateToRunning = {
                                runningViewModel.resetTimer()
                                navController.navigate("running") {
                                    popUpTo("menu") { inclusive = true }
                                }
                            },
                            onNavigateToPace = { navController.navigate("pace") },
                            onNavigateToAny = {
                                val putDataMapRequest = PutDataMapRequest.create("/test")
                                putDataMapRequest.dataMap.putInt("heart_rate_key", 125)
                                val putDataReq = putDataMapRequest.asPutDataRequest()
                                putDataReq.setUrgent()
                                val putTask = Wearable.getDataClient(this@MainActivity).putDataItem(putDataReq)
                                putTask.addOnSuccessListener {
                                    // 성공적으로 전송됨
                                    Log.d(TAG, "onCreate: success")
                                }
                                putTask.addOnFailureListener {
                                    // 전송 실패
                                    Log.d(TAG, "onCreate: fail")
                                }
//                                val putDataTask = dataClient.putDataItem(putDataReq)
//                                try {
//                                    Tasks.await(putDataTask).apply {
//                                        Log.d("Updatehr in apply","125")
//                                    }
//                                } catch (e: ExecutionException) {
//                                    Log.d("UpdateLight", "updateCalories: Failure ${e.printStackTrace()}")
//                                } catch (e: InterruptedException) {
//                                    Log.d("UpdateLight", "updateCalories: Failure ${e.printStackTrace()}")
//                                }
                            },
                            buttonsEnabled = menuButtonsEnabled
                        )
                    }

                    // 시작 화면에서 페이스 설정 없이 시작하는 경우
                    composable("pace") {
                        PaceScreen(
                            onPaceSelected = { pace ->
                                savedPace = pace
                                runningViewModel.resetTimer()
                                navController.navigate("running/$pace") {
                                    popUpTo("menu") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 페이스 설정 후 시작하는 경우
                    composable("running") {
                        RunningScreen(
                            viewModel = runningViewModel,
                            savedState = Triple(savedTopIndex, savedLeftIndex, savedRightIndex),
                            onPauseClick = { mode, data, topIndex, leftIndex, rightIndex, currentRunningData ->
                                savedTopIndex = topIndex
                                savedLeftIndex = leftIndex
                                savedRightIndex = rightIndex
                                savedRunningData = currentRunningData

                                navController.navigate("pause/${mode.name}/$data") {
                                    popUpTo("running") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // Running Screen (with pace)
                    composable("running/{pace}") { backStackEntry ->
                        val pace = backStackEntry.arguments?.getString("pace") ?: "0:00"
                        RunningScreen(
                            viewModel = runningViewModel,
                            pace = pace,
//                            runningData = savedRunningData,
                            savedState = Triple(savedTopIndex, savedLeftIndex, savedRightIndex),
                            onPauseClick = { mode, data, topIndex, leftIndex, rightIndex, currentRunningData ->
                                savedTopIndex = topIndex
                                savedLeftIndex = leftIndex
                                savedRightIndex = rightIndex
                                savedPace = pace
                                savedRunningData = currentRunningData

                                navController.navigate("pause/${mode.name}/$data") {
                                    popUpTo("running/{pace}") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // 일시정지 화면에서 러닝,결과 화면으로 이동 (백스택x)
                    composable("pause/{mode}/{data}") { backStackEntry ->
                        val mode = DisplayMode.valueOf(
                            backStackEntry.arguments?.getString("mode") ?: "TIME"
                        )
                        val data = backStackEntry.arguments?.getString("data") ?: ""

                        // 현재 저장된 거리에서 progress 계산
                        val progression =
                            savedRunningData.distance.toFloatOrNull()?.let { distance ->
                                (distance % 1.0).toFloat()
                            } ?: 0f

                        PauseScreen(
                            displayMode = mode,
                            displayData = data,
                            progress = progression,
                            onStartClick = {
                                // 타이머 재시작
                                runningViewModel.startTimer()

                                // savedPace 값에 따라 올바른 running 화면으로 이동
                                if (savedPace != "0:00") {
                                    navController.navigate("running/$savedPace") {
                                        popUpTo("pause/{mode}/{data}") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate("running") {
                                        popUpTo("pause/{mode}/{data}") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onStopClick = {
                                val finalDistance =
                                    String.format("%.1f", runningViewModel.distance.value)
                                val finalTime = runningViewModel.formattedTime.value
                                val finalAvgPace = runningViewModel.avgPace.value
                                val finalMaxHeartRate = runningViewModel.maxHeartRate.value
                                val finalAvgHeartRate = runningViewModel.avgHeartRate.value
                                val applicationContext = this@MainActivity.applicationContext

                                // 위치 추적 서비스 중지 후 GPX 파일 생성 (비동기로 처리)
                                lifecycleScope.launch {
                                    try {
                                        val result = runningViewModel.createGpxFile(
                                            applicationContext,
                                            "러닝 ${
                                                SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm",
                                                    Locale.getDefault()
                                                ).format(Date())
                                            }"
                                        )
                                        result.fold(
                                            onSuccess = { fileId ->
                                                Log.d("GPX", "GPX 파일 생성 성공: ID=$fileId")
                                            },
                                            onFailure = { error ->
                                                Log.e("GPX", "GPX 파일 생성 실패: ${error.message}")
                                            }
                                        )
                                    } catch (e: Exception) {
                                        Log.e("GPX", "GPX 파일 생성 중 예외 발생: ${e.message}", e)
                                    }

                                    // 생성 완료 후 화면 전환 (UI 스레드에서 실행)
                                    withContext(Dispatchers.Main) {
                                        // 상태 초기화
                                        savedTopIndex = 0
                                        savedLeftIndex = 1
                                        savedRightIndex = 2
                                        savedPace = "0:00"
                                        savedRunningData = RunningData()

                                        navController.navigate("result/$finalDistance/$finalTime/$finalAvgPace/$finalMaxHeartRate/$finalAvgHeartRate") {
                                            popUpTo(0) { inclusive = true }
                                        }

                                        // 타이머 리셋
                                        runningViewModel.resetTimer()
                                    }
                                }
                            })
                    }

                    // 결과 화면
                    composable(
                        "result/{distance}/{time}/{avgPace}/{maxHeartRate}/{avgHeartRate}"
                    ) { backStackEntry ->
                        val distance =
                            backStackEntry.arguments?.getString("distance") ?: "0.0"
                        val time = backStackEntry.arguments?.getString("time") ?: "0:00:00"
                        val avgPace =
                            backStackEntry.arguments?.getString("avgPace") ?: "--'--\""
                        val maxHeartRate =
                            backStackEntry.arguments?.getString("maxHeartRate")
                                ?.toIntOrNull() ?: 0
                        val avgHeartRate =
                            backStackEntry.arguments?.getString("avgHeartRate")
                                ?.toIntOrNull() ?: 0

                        ResultScreen(
                            distance = distance,
                            time = time,
                            avgPace = avgPace,
                            maxHeartRate = maxHeartRate,
                            avgHeartRate = avgHeartRate,
                            onClick = {
                                navController.navigate("menu") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun startConnectionMonitoring() {
        connectionCheckHandler = Handler(Looper.getMainLooper())
        connectionCheckHandler?.postDelayed(connectionCheckRunnable, CONNECTION_CHECK_INTERVAL)
        Log.d("MainActivity", "주기적 연결 상태 모니터링 시작")
    }

    private fun stopConnectionMonitoring() {
        connectionCheckHandler?.removeCallbacks(connectionCheckRunnable)
        connectionCheckHandler = null
        Log.d("MainActivity", "주기적 연결 상태 모니터링 중지")
    }

    override fun onDestroy() {
        stopConnectionMonitoring()
        super.onDestroy()
    }

    private fun checkPhoneConnectionAndUpdateUI() {
        // 1단계: 블루투스 어댑터 확인
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // 블루투스가 꺼져 있으면 연결되지 않은 것으로 간주
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.d("MainActivity", "블루투스가 비활성화되어 있음 - 연결 없음")
            updateUIForDisconnectedState()
            return
        }

        // 2단계: Wear OS 노드 확인
        val nodeClient = Wearable.getNodeClient(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val nodes = Tasks.await(nodeClient.connectedNodes)

                withContext(Dispatchers.Main) {
                    // 노드가 있고 블루투스도 켜져 있으면 연결된 것으로 간주
                    if (nodes.isNotEmpty()) {
                        Log.d("MainActivity", "Phone connected: ${nodes.size} nodes found, 블루투스 활성화됨")
                        updateUIForConnectedState(nodes)
                    } else {
                        Log.d("MainActivity", "연결된 노드 없음")
                        updateUIForDisconnectedState()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking nodes: ${e.message}", e)
                // 오류 발생 시 독립 모드로 동작
                withContext(Dispatchers.Main) {
                    updateUIForDisconnectedState()
                }
            }
        }

//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val nodes = Tasks.await(nodeClient.connectedNodes)
//
//                withContext(Dispatchers.Main) {
//                    if (nodes.isNotEmpty()) {
//                        Log.d("MainActivity", "Phone connected: ${nodes.size} nodes found")
//                        // 폰 연결됨 - 버튼 비활성화, 데이터 동기화 요청
//                        menuButtonsEnabled = false
//
//                        // 폰에 연결됨을 알리기 위해 IDLE 상태 설정
//                        nodes.forEach { node ->
//                            sendStateToPhone(node.id, DataLayerListenerService.STATE_IDLE)
//                        }
//                    } else {
//                        Log.d("MainActivity", "No phone connected - enabling standalone mode")
//                        // 폰 연결 안됨 - 독립 모드 활성화 (버튼 활성화)
//                        menuButtonsEnabled = true
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("MainActivity", "Error checking nodes: ${e.message}", e)
//                // 오류 발생 시 독립 모드로 동작
//                withContext(Dispatchers.Main) {
//                    menuButtonsEnabled = true
//                }
//            }
//        }
    }

    // 연결되지 않은 상태 UI 업데이트
    private fun updateUIForDisconnectedState() {
        menuButtonsEnabled = true
        // 필요한 경우 UI 갱신을 위한 추가 코드
        Log.d("MainActivity", "독립 모드로 전환 - 버튼 활성화")
    }

    // 연결된 상태 UI 업데이트
    private fun updateUIForConnectedState(nodes: List<Node>) {
        menuButtonsEnabled = false

        // 폰에 연결됨을 알리기 위해 IDLE 상태 설정
        nodes.forEach { node ->
            sendStateToPhone(node.id, DataLayerListenerService.STATE_IDLE)
        }

        Log.d("MainActivity", "폰 연결 모드 - 버튼 비활성화")
    }

    // 폰에 상태 전송 메서드
    private fun sendStateToPhone(nodeId: String, state: Int) {
        val messageClient = Wearable.getMessageClient(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 상태 값을 바이트 배열로 변환
                val payload = ByteArray(1).apply { this[0] = state.toByte() }

                messageClient.sendMessage(nodeId, DataLayerListenerService.RUNNING_STATE_PATH, payload)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Successfully sent state $state to phone")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Failed to send state to phone: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error sending state to phone: ${e.message}", e)
            }
        }
    }

    private fun setupMessageReceivers() {
        // Test message receiver
        val testMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getStringExtra("message") ?: "메시지 없음"
                Log.d("MainActivity", "테스트 메시지 수신: $message")
                Toast.makeText(this@MainActivity, "📱➡️⌚ $message", Toast.LENGTH_LONG).show()
            }
        }

        // 러닝 상태 브로드캐스트 리시버들
        val runStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("MainActivity", "러닝 상태 브로드캐스트 수신: ${intent.action}")

                when (intent.action) {
                    DataLayerListenerService.ACTION_START_RUNNING -> {
                        // 러닝 화면으로 이동
                        navController?.navigate("running") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    DataLayerListenerService.ACTION_PAUSE_RUNNING -> {
                        // 진행 중이던 상태를 일시정지하고 PauseScreen으로 이동
                        runningViewModel.pauseTimer()
                        val mode = DisplayMode.TIME
                        val data = runningViewModel.formattedTime.value
                        navController?.navigate("pause/${mode.name}/$data") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    DataLayerListenerService.ACTION_FINISH_RUNNING -> {
                        // 러닝 종료하고 ResultScreen으로 이동
                        val distance = String.format("%.1f", runningViewModel.distance.value)
                        val time = runningViewModel.formattedTime.value
                        val avgPace = runningViewModel.avgPace.value
                        val maxHeartRate = runningViewModel.maxHeartRate.value
                        val avgHeartRate = runningViewModel.avgHeartRate.value

                        navController?.navigate("result/$distance/$time/$avgPace/$maxHeartRate/$avgHeartRate") {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    "com.D107.runmate.watch.ACTION_DISABLE_BUTTONS" -> {
                        // 메뉴 화면 버튼 비활성화
                        menuButtonsEnabled = false
                        Log.d("MainActivity", "버튼 비활성화 요청 수신")
                    }

                    "com.D107.runmate.watch.ACTION_ENABLE_BUTTONS" -> {
                        // 메뉴 화면 버튼 활성화
                        menuButtonsEnabled = true
                        Log.d("MainActivity", "버튼 활성화 요청 수신")
                    }

                    "com.D107.runmate.watch.ACTION_RETURN_TO_MENU" -> {
                        // 메뉴 화면으로 자동 이동
                        navController?.navigate("menu") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }

        // 리시버 등록
        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(
                testMessageReceiver,
                IntentFilter("com.D107.runmate.watch.TEST_MESSAGE")
            )

            // 러닝 상태 리시버 등록
            registerReceiver(
                runStateReceiver,
                IntentFilter(DataLayerListenerService.ACTION_START_RUNNING)
            )
            registerReceiver(
                runStateReceiver,
                IntentFilter(DataLayerListenerService.ACTION_PAUSE_RUNNING)
            )
            registerReceiver(
                runStateReceiver,
                IntentFilter(DataLayerListenerService.ACTION_FINISH_RUNNING)
            )
            registerReceiver(
                runStateReceiver,
                IntentFilter("com.D107.runmate.watch.ACTION_DISABLE_BUTTONS")
            )
            registerReceiver(
                runStateReceiver,
                IntentFilter("com.D107.runmate.watch.ACTION_RETURN_TO_MENU")
            )
            registerReceiver(
                runStateReceiver,
                IntentFilter("com.D107.runmate.watch.ACTION_ENABLE_BUTTONS")
            )
        }
    }


    private fun checkPhoneConnection() {
        val nodeClient = Wearable.getNodeClient(this)
        lifecycleScope.launch(Dispatchers.IO) {  // Dispatchers.IO 추가
            try {
                val nodes = Tasks.await(nodeClient.connectedNodes)
                withContext(Dispatchers.Main) {  // UI 업데이트는 메인 스레드로
                    Log.d("MainActivity", "Connected nodes: ${nodes.size}")
                    if (nodes.isNotEmpty()) {
                        Log.d("MainActivity", "BLE 연결 성공: 폰과 워치 연결됨")
                        Toast.makeText(this@MainActivity, "폰과 연결됨", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("MainActivity", "BLE 연결 실패: 연결된 기기 없음")
                    }
                }

                nodes.forEach { node ->
                    Log.d("MainActivity", "Connected to node: ${node.displayName}, id: ${node.id}")
                    requestTokenFromPhone(node.id)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking connected nodes: ${e.message}", e)
            }
        }
    }

    private fun requestTokenFromPhone(nodeId: String) {
        val messageClient = Wearable.getMessageClient(this)
        lifecycleScope.launch(Dispatchers.IO) {  // Dispatchers.IO 추가
            try {
                Log.d("MainActivity", "Requesting token from phone...")
                messageClient.sendMessage(nodeId, "/request_token", ByteArray(0))
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Token request sent to node: $nodeId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Failed to send token request: ${e.message}", e)
                    }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to send token request: ${e.message}", e)
            }
        }
    }


    // 블루투스 연결 성공 후 심박수 전송 시작
    private fun connectToApp(deviceAddress: String) {
        lifecycleScope.launch {
            val bluetoothService = BluetoothService(applicationContext)
            if (bluetoothService.connectToDevice(deviceAddress)) {
                // 연결 성공 - 심박수만 전송 모드 시작
                Log.d("Bluetooth", "앱에 연결 성공")
                // 수정된 부분: 클래스 멤버 변수 사용
                runningViewModel.startHeartRateOnlyTracking(applicationContext)
            } else {
                // 연결 실패
                Log.e("Bluetooth", "앱에 연결 실패")
            }
        }
    }

    companion object {
        private const val BODY_SENSOR_PERMISSION_REQUEST_CODE = 1
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true, showBackground = true)
@Composable
fun ScreenPreview() {
    RunMateTheme {
        PauseScreen()
    }
}