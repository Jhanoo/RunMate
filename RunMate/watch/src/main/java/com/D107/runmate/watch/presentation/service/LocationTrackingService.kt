package com.D107.runmate.watch.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.D107.runmate.watch.R
import com.D107.runmate.watch.domain.model.GpxTrackPoint
import com.D107.runmate.watch.domain.repository.GpxRepository
import com.D107.runmate.watch.presentation.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var gpxRepository: GpxRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val TAG = "LocationTrackingService"

        // 위치 수집 간격 (5초)
        private const val LOCATION_UPDATE_INTERVAL = 5000L

        // Service 시작/종료를 위한 Intent actions
        const val ACTION_START = "com.D107.runmate.watch.START_TRACKING"
        const val ACTION_STOP = "com.D107.runmate.watch.STOP_TRACKING"

        // 심박수 가져오는 방법
        private var lastHeartRate = 0

        // 현재 페이스 가져오는 방법
        private var currentPace = "0'00\""

        fun updateHeartRate(heartRate: Int) {
            lastHeartRate = heartRate
        }

        fun updatePace(pace: String) {
            currentPace = pace
        }
    }

    // 위치 업데이트 콜백
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                val now = Date()

                // 위치 데이터 수집 (5초마다 호출됨)
                val trackPoint = GpxTrackPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    elevation = location.altitude,
                    time = now,
                    heartRate = lastHeartRate,
                    cadence = 180, // 고정 값
                    pace = currentPace
                )

                // Repository에 트랙 포인트 추가
                serviceScope.launch {
                    gpxRepository.addTrackPoint(trackPoint)
                    Log.d(TAG, "트랙 포인트 저장: $trackPoint")
                }

                lastLocation = location
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun start() {
        // 알림 생성 및 Foreground Service 시작
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // 위치 업데이트 시작
        startLocationUpdates()

        Log.d(TAG, "위치 추적 서비스 시작됨")
    }

    private fun stop() {
        // 위치 업데이트 중지
        stopLocationUpdates()

        // 서비스 중지
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "위치 추적 서비스 중지됨")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "위치 추적",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "러닝 중 위치를 추적합니다"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("러닝 기록 중...")
            .setContentText("위치 데이터를 수집하고 있습니다")
            .setSmallIcon(R.drawable.pause)  // 적절한 아이콘으로 변경
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        // 5초마다 위치 업데이트 요청 설정
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(5000L)  // 5초
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        Log.d(TAG, "위치 업데이트 시작 (5초 간격)")
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}