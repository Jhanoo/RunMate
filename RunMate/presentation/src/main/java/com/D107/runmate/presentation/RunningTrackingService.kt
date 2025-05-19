package com.D107.runmate.presentation

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkBuilder
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.model.socket.ConnectionStatus
import com.D107.runmate.domain.model.socket.SocketAuth
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.running.RunningRepository
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.domain.usecase.running.EndRunningUseCase
import com.D107.runmate.domain.usecase.socket.ConnectSocketUseCase
import com.D107.runmate.domain.usecase.socket.DisconnectSocketUseCase
import com.D107.runmate.domain.usecase.socket.IsSocketConnectedUseCase
import com.D107.runmate.domain.usecase.socket.JoinGroupSocketUseCase
import com.D107.runmate.domain.usecase.socket.LeaveGroupSocketUseCase
import com.D107.runmate.domain.usecase.socket.ObserveLocationUpdatesUseCase
import com.D107.runmate.domain.usecase.socket.ObserveMemberLeavedUseCase
import com.D107.runmate.domain.usecase.socket.SendLocationUpdateUseCase
import com.D107.runmate.presentation.group.viewmodel.SocketAuthParcelable
import com.D107.runmate.presentation.group.viewmodel.toDomain
import com.D107.runmate.presentation.running.Coord2AddressState
import com.D107.runmate.presentation.running.RunningEndState
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.CommonUtils.convertDateTime
import com.D107.runmate.presentation.utils.LocationUtils.trackingLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.time.times

@AndroidEntryPoint
class RunningTrackingService : Service(), TextToSpeech.OnInitListener {
    private val TAG = "RunningService"
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "running_tracker_channel"

    @Inject
    lateinit var repository: RunningTrackingRepository

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    @Inject
    lateinit var getCoord2AddressUseCase: GetCoord2AddressUseCase
    @Inject
    lateinit var endRunningUseCase: EndRunningUseCase
    @Inject
    lateinit var connectSocketUseCase: ConnectSocketUseCase
    @Inject
    lateinit var disconnectSocketUseCase: DisconnectSocketUseCase
    @Inject
    lateinit var isSocketConnectedUseCase: IsSocketConnectedUseCase
    @Inject
    lateinit var joinGroupSocketUseCase: JoinGroupSocketUseCase
    @Inject
    lateinit var sendLocationUseCase: SendLocationUpdateUseCase
    @Inject
    lateinit var leaveGroupSocketUseCase: LeaveGroupSocketUseCase
    @Inject
    lateinit var observeLocationUpdatesUseCase: ObserveLocationUpdatesUseCase
    @Inject
    lateinit var observeMemberLeavedUseCase: ObserveMemberLeavedUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var runningJob: RunningJobState = RunningJobState.Initial
    private var timeTrackingJob: Job? = null

    private var sensorManager: SensorManager? = null
    private var stepCountInWindow = 0 // 5초 동안의 걸음 수
    private var windowStartTime = 0L
    private val timeWindow = 5_000L
    private var currentGroupId: String? = null
    private var currentGroupLeaderId: String? = null
    private var currentSocketAuth: SocketAuthParcelable? = null
    private var isGroupRunningMode: Boolean = false
    var cadence = 0
        private set

    private var isTracking = false

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Initial)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private var socketConnectionObserverJob: Job? = null

    private var leaveGroupJob: Job? = null

    private val _endRunning = MutableSharedFlow<RunningEndState>()

    private var tts: TextToSpeech? = null
    private var ttsJob: Job? = null

    private var vibrateJob: Job? = null
    private lateinit var vibrator: Vibrator

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeSocketConnection()
        tts = TextToSpeech(this, this)
        initVibrator()
    }

    private fun observeSocketConnection() {
        if (socketConnectionObserverJob?.isActive == true) return

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("onStartCommand RunningService $currentGroupId")

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                currentGroupId = intent.getStringExtra(EXTRA_GROUP_ID)
                isGroupRunningMode = currentGroupId != null
                currentGroupLeaderId = intent.getStringExtra(EXTRA_GROUP_LEADER_ID)
                if (currentSocketAuth == null) {
                    currentSocketAuth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            EXTRA_SOCKET_AUTH,
                            SocketAuthParcelable::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<SocketAuthParcelable>(EXTRA_SOCKET_AUTH)
                    }
                }
                startForegroundService()

                if (isGroupRunningMode && currentGroupId != null) {
                    if (!isSocketConnectedUseCase().value) { // 현재 연결 안되어있으면 연결 시도
                        currentSocketAuth?.let { socketAuth ->
                            val auth = socketAuth.toDomain()
                            serviceScope.launch {
                                connectSocketUseCase(auth).collectLatest { status ->
                                    Timber.d("Connection attempt status for group $currentGroupId: $status")
                                    if (status is ConnectionStatus.Connected) {
                                        if (isSocketConnectedUseCase().value) {
                                            joinGroupSocket()
                                        } else {
                                            Timber.w("Socket not connected. Cannot join group.")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        joinGroupSocket()
                        Timber.d("Socket already connected, ensuring group join for: $currentGroupId")

                    }
                }
            }

            ACTION_PAUSE_SERVICE -> pauseService()
            ACTION_STOP_SERVICE -> stopService()
            ACTION_SOUND -> toggleSound()
            ACTION_VIBRATE -> toggleVibrate()
        }

        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationTracking()

        startTimeTracking()
        startTracking(this)

        if(repository.isSound.value) {
            startTts()
        } else {
            stopTts()
        }

        if(repository.isVibration.value) {
            startVibration()
        } else {
            stopVibration()
        }
    }

    private fun stopService() {
        stopTimeTracking()
        stopLocationTracking()
        stopTracking()

        if(repository.recordSize.value > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.finishTracking().collectLatest {
                    if (it) {
                        val record = repository.runningRecord.value
                        val location = repository.userLocation.value
                        val recordSize = repository.recordSize.value
                        if (location is UserLocationState.Exist && record is RunningRecordState.Exist && recordSize > 0) {
                            val address = getAddress(
                                location.locations.first().longitude,
                                location.locations.first().latitude
                            )
                            address?.let {
                                val lastRecord = record.runningRecords.last()
                                val avgPace = 16.6667 / lastRecord.avgSpeed
                                val met = if(avgPace > 20*60) 1.0
                                else if(avgPace > 10*60) 2.5
                                else if(avgPace > 7.5*60) 5.0
                                else if(avgPace > 5*60) 7.0
                                else 10.0
                                val weight = dataStoreRepository.weight.first() ?: 0.0
                                val calories = (repository.time.value/60) * met * 3.5 * weight / 200
                                endRunning(
                                    0.0,
                                    lastRecord.cadenceSum / recordSize,
                                    lastRecord.altitudeSum / recordSize,
                                    avgPace,
                                    calories,
                                    repository.courseId.value,
                                    (lastRecord.distance).toDouble(),
                                    convertDateTime(lastRecord.currentTime),
                                    it,
                                    convertDateTime(record.runningRecords.first().currentTime),
                                    currentGroupId
                                )
                            }
                        } else {
                            Timber.d("기록이 없습니다 recordSize 0")
                        }
                    } else {
                        Timber.d("write fail")
                    }
                }
            }
        } else {
            // 종료시키기
            stopForeground(true)
            stopSelf()
            repository.setTrackingStatus(TrackingStatus.INITIAL)
        }

        CoroutineScope(Dispatchers.Default).launch {
            _endRunning.collectLatest {
                if (it is RunningEndState.Success) {
                    repository.setTrackingStatus(TrackingStatus.STOPPED)
                    repository.setHistoryId(it.historyId)
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
    }

    private fun pauseService() {
        stopTimeTracking()
        stopLocationTracking()
        stopTracking()
    }

    private suspend fun getAddress(lon: Double, lat: Double): String? {
        return getCoord2AddressUseCase(lon, lat)
            .first { it is ResponseStatus.Success }
            .let { status ->
                when (status) {
                    is ResponseStatus.Success -> status.data.address_name
                    else -> null
                }
            }
    }

    private suspend fun endRunning(
        avgBpm: Double,
        avgCadence: Double,
        avgElevation: Double,
        avgPace: Double,
        calories: Double,
        courseId: String?,
        distance: Double,
        endTime: String,
        startLocation: String,
        startTime: String,
        groupId: String? = null
    ) {
        endRunningUseCase(
            avgBpm,
            avgCadence,
            avgElevation,
            avgPace,
            calories,
            courseId,
            distance,
            endTime,
            startLocation,
            startTime,
            groupId
        )
            .onStart {
            }
            .catch { e ->
                Timber.e("runningend error catch ${e.message}")
                _endRunning.emit(RunningEndState.Error(e.message ?: "알 수 없는 오류가 발생했습니다"))
            }
            .collect { status ->
                when (status) {
                    is ResponseStatus.Success -> {
                        _endRunning.emit(RunningEndState.Success(status.data.historyId))
                        repository.setCourseId(null)
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("runningend error ${status.error.message}")
                        _endRunning.emit(RunningEndState.Error(status.error.message))
                        repository.setCourseId(null)
                    }
                }
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Running Tracker"
            val descriptionText = "Tracks your running activity"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("러닝 준비 중")
            .setSmallIcon(R.drawable.image_tonie_small)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startTimeTracking() {
        if (timeTrackingJob?.isActive == true) return

        timeTrackingJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                repository.incrementTime()
            }
        }
    }

    private fun stopTimeTracking() {
        timeTrackingJob?.cancel()
        timeTrackingJob = null
    }

    private fun startLocationTracking() {
        if (runningJob is RunningJobState.Active) return

        val job = serviceScope.launch {
            try {
                trackingLocation(this@RunningTrackingService)
                    .catch { e ->
                        Log.e(TAG, "Error in location tracking: ${e.message}", e)
                        runningJob = RunningJobState.Error("위치 추적 중 오류 발생: ${e.message}")
                        repository.setTrackingStatus(TrackingStatus.PAUSED)
                    }
                    .collectLatest { location ->
                        val locationModel = LocationModel(
                            location.latitude,
                            location.longitude,
                            location.altitude,
                            location.speed
                        )
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - windowStartTime > timeWindow) {
                            cadence = 0
                        }
                        if (isSocketConnectedUseCase().value) {
                            sendLocationUseCase(location.latitude, location.longitude)
                        }
                        repository.processLocationUpdate(locationModel, cadence)
                        repository.setTrackingStatus(TrackingStatus.RUNNING)
                    }
            } catch (e: Exception) {
                runningJob = RunningJobState.Error("위치 추적 작업 중 오류 발생: ${e.message}")
                repository.setTrackingStatus(TrackingStatus.PAUSED)
            }
        }

        runningJob = RunningJobState.Active(job)
        updateNotification(runningJob)
    }

    private fun stopLocationTracking() {
        if (runningJob is RunningJobState.Active) {
            (runningJob as RunningJobState.Active).job.cancel()
            runningJob = RunningJobState.None
            updateNotification(runningJob)
        }
    }

    private fun toggleSound() {
        repository.toggleIsSound()
        if(repository.isSound.value && repository.goalPace.value != null) {
            startTts()
        }else if(repository.isSound.value == false && repository.goalPace.value != null) {
            stopTts()
        }
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun toggleVibrate() {
        repository.toggleIsVibration()
        if(repository.isVibration.value && repository.goalPace.value != null) {
            startVibration()
        } else if(repository.isVibration.value == false && repository.goalPace.value != null) {
            stopVibration()
        }
    }

    private fun startVibration() {
        val goalPace = repository.goalPace.value
        if(repository.isVibration.value && goalPace != null) {
            vibrateJob = CoroutineScope(Dispatchers.Main).launch {
                repository.runningRecord.collectLatest { state ->
                    if(state is RunningRecordState.Exist) {
                        state.runningRecords.last().let {
                            val avgPace = 16.6667 / it.avgSpeed
                            if(goalPace > avgPace) {
                                Timber.d("goalPace: first $goalPace, avgPace: $avgPace")
                                triggerVibration()
                            } else if(goalPace < avgPace) {
                                Timber.d("goalPace: second $goalPace, avgPace: $avgPace")
                                triggerVibration()
                            } else {
                                Timber.d("정상속도거나 너무 빠르거나 느림")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopVibration() {
        vibrator.cancel()
        vibrateJob?.cancel()
    }

    private fun triggerVibration() {
        Timber.d("vibration")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromGroupSocket()
        serviceScope.cancel()
        runningJob = RunningJobState.None
        tts?.stop()
        tts?.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    sealed class RunningJobState {
        object Initial : RunningJobState()
        data class Active(val job: Job) : RunningJobState()
        data class Error(val message: String) : RunningJobState()
        object None : RunningJobState()
    }

    companion object {
        const val ACTION_START_SERVICE = "action_start_service"
        const val ACTION_STOP_SERVICE = "action_stop_service"
        const val ACTION_PAUSE_SERVICE = "action_pause_service"

        const val ACTION_VIBRATE = "ACTION_VIBRATE"
        const val ACTION_SOUND = "ACTION_SOUND"
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        const val EXTRA_GROUP_LEADER_ID = "EXTRA_GROUP_LEADER_ID"
        const val EXTRA_SOCKET_AUTH = "EXTRA_Socket_AUTH"


        fun startService(
            context: Context,
            groupId: String? = null,
            groupLeaderId: String? = null,
            socketAuth: SocketAuthParcelable? = null
        ) { // groupId를 nullable String으로 추가
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_START_SERVICE
                if (groupId != null && groupLeaderId != null) {
                    putExtra(EXTRA_GROUP_ID, groupId)
                    putExtra(EXTRA_GROUP_LEADER_ID, groupLeaderId)
                }
                if (socketAuth != null) {
                    putExtra(EXTRA_SOCKET_AUTH, socketAuth)

                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context, groupId: String? = null) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_STOP_SERVICE
                if (groupId != null) {
                    putExtra(EXTRA_GROUP_ID, groupId)
                }
            }
            context.startService(intent)
        }

        fun pauseService(context: Context, groupId: String? = null) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
                if (groupId != null) {
                    putExtra(EXTRA_GROUP_ID, groupId)
                }
            }
            context.startService(intent)
        }

        fun toggleSoundService(context: Context) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_SOUND
            }
            context.startService(intent)
        }

        fun toggleVibrateService(context: Context) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_VIBRATE
            }
            context.startService(intent)
        }
    }

    private fun createPendingIntent(
        action: String,
        requestCode: Int,
        socketAuth: SocketAuthParcelable? = null
    ): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getNotificationBuilder(state: RunningJobState): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_drawer_menu)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return when (state) {
            is RunningJobState.Active -> {
                builder.setContentTitle("달리기 진행 중")
                    .addAction(
                        R.drawable.ic_action_vibrate,
                        "진동",
                        createPendingIntent(ACTION_VIBRATE, 0)
                    )
                    .addAction(
                        R.drawable.ic_action_running,
                        "러닝중",
                        createPendingIntent(ACTION_PAUSE, 1)
                    )
                    .addAction(
                        R.drawable.ic_action_sound,
                        "소리",
                        createPendingIntent(ACTION_SOUND, 2)
                    )
                    .setStyle(getMediaStyle(0, 1, 2))
            }

            is RunningJobState.None -> {
                builder.setContentTitle("달리기 일시정지")
                    .addAction(
                        R.drawable.ic_action_start,
                        "일시정지",
                        createPendingIntent(ACTION_START, 3)
                    )
                    .addAction(R.drawable.ic_action_stop, "종료", createPendingIntent(ACTION_STOP, 4))
                    .setStyle(getMediaStyle(0, 1))
            }

            else -> {
                builder.setContentTitle("달리기 트래킹 오류 발생")
                    .addAction(R.drawable.ic_action_stop, "종료", createPendingIntent(ACTION_STOP, 5))
                    .setStyle(getMediaStyle(0))
            }
        }
    }

    private fun getMediaStyle(vararg indices: Int): NotificationCompat.Style {
        return androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(*indices)
    }

    fun updateNotification(state: RunningJobState) {
        val notification = getNotificationBuilder(state).build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, notification)
    }

    fun startTracking(context: Context) {
        if (isTracking) return
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)?.let { sensor ->
            sensorManager?.registerListener(stepListener, sensor, SensorManager.SENSOR_DELAY_UI)
            isTracking = true
            windowStartTime = System.currentTimeMillis()
        }
    }

    fun stopTracking() {
        sensorManager?.unregisterListener(stepListener)
        sensorManager = null
        isTracking = false
        stepCountInWindow = 0
        cadence = 0
    }

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR && event.values[0] == 1.0f) {
                stepCountInWindow++
                calculateCadence()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun calculateCadence() {
        val currentTime = System.currentTimeMillis()

        // 5초마다 케이던스 계산
        if (currentTime - windowStartTime >= timeWindow) {
            cadence = (stepCountInWindow * 12) // 5초 걸음 수 → 분당 걸음 수(60/5=12)
            stepCountInWindow = 0 // 윈도우 초기화
            windowStartTime = currentTime // 다음 윈도우 시작 시간 업데이트
        }
    }

    private fun connectToGroupSocket(groupId: String) {
        // viewModel.joinGroupSocket() 같은 로직을 Service 내부에서 실행
        // 또는 Service에서 Repository를 통해 소켓 연결 상태를 관리하고,
        // Repository가 실제 소켓 통신을 담당하도록 설계 (더 클린한 접근)
        Timber.d("그룹 소켓 연결 시도: $groupId")
        // 실제 소켓 연결 코드...
    }

    private fun disconnectFromGroupSocket() {
        if (isGroupRunningMode) {
//            disconnectSocketUseCase()
            currentGroupId = null
            currentSocketAuth = null
            currentGroupLeaderId = null
        }
    }

    private fun joinGroupSocket() {
        serviceScope.launch {
            joinGroupSocketUseCase(currentGroupId!!)
            Timber.d("here!!!!")
            observeMemberLeavedUseCase().collect { leaveGroupMessage ->
                Timber.d("${leaveGroupMessage}")
                if(leaveGroupMessage.userId==currentGroupLeaderId){
                    val channelId = "GROUP_RUN_TERMINATION_CHANNEL"
                    val notificationId = System.currentTimeMillis().toInt()

                    val pendingIntent = NavDeepLinkBuilder(this@RunningTrackingService)
                        .setComponentName(MainActivity::class.java)
                        .setGraph(R.navigation.nav_graph)
                        .setDestination(R.id.runningEndFragment)
                        .setArguments(Bundle().apply {
                            putString("sourceScreen", "GROUP_RUNNING_FRAGMENT")
                        })
                        .createPendingIntent()

                    val builder = NotificationCompat.Builder(this@RunningTrackingService, channelId)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle("그룹 달리기 종료")
                        .setContentText("그룹장에 의해 그룹 달리기가 종료되었습니다. 기록이 저장됩니다.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_EVENT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true) // Heads-up 알림으로 표시

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@RunningTrackingService, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            NotificationManagerCompat.from(this@RunningTrackingService).notify(notificationId, builder.build())
                        } else {
                            Log.w("Notification", "POST_NOTIFICATIONS permission not granted for termination alert.")
                        }
                    } else {
                        NotificationManagerCompat.from(this@RunningTrackingService).notify(notificationId, builder.build())
                    }
                    stopService()
                }
            }
        }
    }

    private fun startTts() {
        val goalPace = repository.goalPace.value
        if(goalPace != null) {
            ttsJob = CoroutineScope(Dispatchers.Main).launch {
                repository.runningRecord.collectLatest { state ->
                    if(state is RunningRecordState.Exist) {
                        state.runningRecords.last().let {
                            val avgPace = 16.6667 / it.avgSpeed
                            if(goalPace > avgPace) {
                                tts?.speak("${goalPace - avgPace} 보다 빨라요", TextToSpeech.QUEUE_FLUSH, null, "tts1")
                            } else if(goalPace < avgPace) {
                                tts?.speak("${avgPace - goalPace} 보다 느려요", TextToSpeech.QUEUE_FLUSH, null, "tts2")
                            } else {
                                Timber.d("정상속도거나 너무 빠르거나 느림")
                            }
                        }
                    }
                }
            }
        } else {
            Timber.d("페이스 설정 안함")
        }
    }

    private fun stopTts() {
        ttsJob?.cancel()
        ttsJob = null
        tts?.stop()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.KOREAN
        }
    }

}