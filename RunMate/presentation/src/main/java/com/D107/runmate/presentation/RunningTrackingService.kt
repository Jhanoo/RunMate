package com.D107.runmate.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.repository.running.RunningRepository
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import com.D107.runmate.domain.usecase.group.GetCoord2AddressUseCase
import com.D107.runmate.domain.usecase.running.EndRunningUseCase
import com.D107.runmate.presentation.running.Coord2AddressState
import com.D107.runmate.presentation.running.RunningEndState
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RunningTrackingService : Service() {
    private val TAG = "RunningService"
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "running_tracker_channel"

    @Inject
    lateinit var repository: RunningTrackingRepository

    @Inject
    lateinit var getCoord2AddressUseCase: GetCoord2AddressUseCase

    @Inject
    lateinit var endRunningUseCase: EndRunningUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var runningJob: RunningJobState = RunningJobState.Initial
    private var timeTrackingJob: Job? = null

    private var sensorManager: SensorManager? = null
    private var stepCountInWindow = 0 // 5초 동안의 걸음 수
    private var windowStartTime = 0L
    private val timeWindow = 5_000L
    var cadence = 0
        private set

    private var isTracking = false

    private val serviceJobIo = SupervisorJob()
    private val serviceScopeIO = CoroutineScope(Dispatchers.IO + serviceJobIo)

//    private val _coord2Address = MutableSharedFlow<Coord2AddressState>()

    private val _endRunning = MutableSharedFlow<RunningEndState>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_SERVICE -> startForegroundService()
            ACTION_PAUSE_SERVICE -> pauseService()
            ACTION_STOP_SERVICE -> stopService()
        }

        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationTracking()

        startTimeTracking()
        startTracking(this)
    }

    private fun stopService() {
        stopTimeTracking()
        stopLocationTracking()
        stopTracking()

        CoroutineScope(Dispatchers.IO).launch {
            repository.finishTracking().collectLatest {
                if (it) {
                    Timber.d("write finish")
                    val record = repository.runningRecord.value
                    val location = repository.userLocation.value
                    val recordSize = repository.recordSize.value
                    if (location is UserLocationState.Exist && record is RunningRecordState.Exist) {
                        Timber.d("coord2Address recordSize ${recordSize}")
                        val address = getAddress(
                            location.locations.first().longitude,
                            location.locations.first().latitude
                        )
                        address?.let {
                            Timber.d("lastRecord")
                            val lastRecord = record.runningRecords.last()
                            endRunning(
                                0.0,
                                lastRecord.cadenceSum / recordSize,
                                lastRecord.altitudeSum / recordSize,
                                16.6667 / lastRecord.avgSpeed,
                                0.0,
                                null,
                                (lastRecord.distance).toDouble(),
                                convertDateTime(lastRecord.currentTime),
                                it,
                                convertDateTime(record.runningRecords.first().currentTime)
                            )
                        }
                    }
                } else {
                    Timber.d("write fail")
                }
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            _endRunning.collectLatest {
                if (it is RunningEndState.Success) {
                    Timber.d("running end ! stop service")
                    repository.setTrackingStatus(TrackingStatus.PAUSED)
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
        startTime: String
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
            startTime
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
                        Timber.d("runningend success")
                        _endRunning.emit(RunningEndState.Success)
                    }

                    is ResponseStatus.Error -> {
                        Timber.d("runningend error ${status.error.message}")
                        _endRunning.emit(RunningEndState.Error(status.error.message))
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
            .setSmallIcon(R.drawable.ic_drawer_menu)
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
//            repository.setTrackingStatus(TrackingStatus.PAUSED)
            updateNotification(runningJob)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        runningJob = RunningJobState.None
        repository.setTrackingStatus(TrackingStatus.STOPPED)
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

        fun startService(context: Context) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }

        fun pauseService(context: Context) {
            val intent = Intent(context, RunningTrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            context.startService(intent)
        }
    }

    private fun createPendingIntent(action: String, requestCode: Int): PendingIntent {
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
        Timber.d("startTracking Cadence")
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)?.let { sensor ->
            sensorManager?.registerListener(stepListener, sensor, SensorManager.SENSOR_DELAY_UI)
            isTracking = true
            windowStartTime = System.currentTimeMillis()
        }
    }

    fun stopTracking() {
        Timber.d("stopTracking Cadence")
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
            Log.d("Cadence", "Current Cadence: $cadence SPM")
        }
    }

}