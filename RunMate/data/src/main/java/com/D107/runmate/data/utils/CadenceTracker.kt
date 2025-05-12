//package com.D107.runmate.data.utils
//
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.util.Log
//import com.D107.runmate.domain.repository.running.RunningTrackingRepository
//import javax.inject.Inject
//
//class CadenceTracker @Inject constructor(
//    private val repository: RunningTrackingRepository
//) {
//    private var sensorManager: SensorManager? = null
//    private var stepCountInWindow = 0 // 5초 동안의 걸음 수
//    private var windowStartTime = 0L
//    var cadence = 0
//        private set
//
//    private var isTracking = false
//
//    fun startTracking(context: Context) {
//        if (isTracking) return
//        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)?.let { sensor ->
//            sensorManager?.registerListener(stepListener, sensor, SensorManager.SENSOR_DELAY_UI)
//            isTracking = true
//            windowStartTime = System.currentTimeMillis()
//        }
//    }
//
//    fun stopTracking() {
//        sensorManager?.unregisterListener(stepListener)
//        sensorManager = null
//        isTracking = false
//        stepCountInWindow = 0
//        cadence = 0
//    }
//
//    private val stepListener = object : SensorEventListener {
//        override fun onSensorChanged(event: SensorEvent) {
//            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR && event.values[0] == 1.0f) {
//                stepCountInWindow++
//
//            }
//            calculateCadence()
//        }
//        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
//    }
//
//    private fun calculateCadence() {
//        val currentTime = System.currentTimeMillis()
//        val timeWindow = 5_000L // 5초
//
//        // 5초마다 케이던스 계산
//        if (currentTime - windowStartTime >= timeWindow) {
//            cadence = (stepCountInWindow * 12) // 5초 걸음 수 → 분당 걸음 수(60/5=12)
//            stepCountInWindow = 0 // 윈도우 초기화
//            windowStartTime = currentTime // 다음 윈도우 시작 시간 업데이트
//            Log.d("Cadence", "Current Cadence: $cadence SPM")
//            repository.addCadence(cadence)
//        }
//    }
//}
