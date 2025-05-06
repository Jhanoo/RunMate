package com.D107.runmate.watch.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.D107.runmate.watch.domain.repository.CadenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CadenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CadenceRepository {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 스텝 감지 센서를 사용할 수 없는 경우를 위한 대체 옵션 추가
    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // 가속도계로 대체
    private var stepCount = 0
    private var lastResetTime = System.currentTimeMillis()
    private var isMonitoring = false

    // 이동 평균을 위한 최근 케이던스 값 저장
    private val recentCadences = mutableListOf<Int>()
    private val MAX_CADENCE_HISTORY = 4 // 최근 4개 값으로 이동 평균 계산

    init {
        Log.d("Cadence", "CadenceRepositoryImpl 초기화됨")
        Log.d(
            "Cadence",
            "사용 가능한 센서: ${sensorManager.getSensorList(Sensor.TYPE_ALL).map { it.name }}"
        )
        Log.d("Cadence", "스텝 센서 사용 가능: ${stepDetector != null}")
    }

    private val stepListener = object : SensorEventListener {
        private var lastTimestamp = 0L
        private var lastX = 0f
        private var lastY = 0f
        private var lastZ = 0f
        private val THRESHOLD = 10.0f // 걸음 감지 임계값

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_DETECTOR -> {
                    stepCount++
                    Log.d("Cadence", "스텝 감지됨: $stepCount")
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    // 가속도계 기반 걸음 감지 (단순 구현)
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val currentTime = System.currentTimeMillis()
                    if (lastTimestamp == 0L) {
                        lastTimestamp = currentTime
                        lastX = x
                        lastY = y
                        lastZ = z
                        return
                    }

                    // 최소 시간 간격 (250ms)
                    if (currentTime - lastTimestamp < 250) return

                    // 가속도 변화량 계산
                    val deltaX = Math.abs(lastX - x)
                    val deltaY = Math.abs(lastY - y)
                    val deltaZ = Math.abs(lastZ - z)

                    // 임계값 이상이면 걸음으로 감지
                    if ((deltaX + deltaY + deltaZ) > THRESHOLD) {
                        stepCount++
                        Log.d("Cadence", "가속도계로 스텝 감지됨: $stepCount")
                        lastTimestamp = currentTime
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.d("Cadence", "센서 정확도 변경: $accuracy")
        }
    }

    override fun getCurrentCadence(): Int {
        if (!isMonitoring) {
            Log.d("Cadence", "모니터링 중이 아닐 때 케이던스 요청됨")
            // 모니터링 중이 아니면 기본값 반환
            return 0
        }
        val currentTime = System.currentTimeMillis()
        val timeElapsedSeconds = (currentTime - lastResetTime) / 1000.0
        Log.d("Cadence", "케이던스 계산 - 경과 시간: ${timeElapsedSeconds}초, 스텝 카운트: $stepCount")

        // 스텝 카운트를 분당 스텝으로 변환 (최소 1초 이상 경과 필요)
        val stepsPerMinute = if (timeElapsedSeconds > 1.0) {
            (stepCount / timeElapsedSeconds * 60).toInt()
        } else {
            // 시간이 너무 짧으면 이전 값 유지 또는 기본값
            if (recentCadences.isNotEmpty()) recentCadences.last() else 0
        }

        // 이동 평균 계산을 위해 저장
        recentCadences.add(stepsPerMinute)
        // 최대 개수 유지
        if (recentCadences.size > MAX_CADENCE_HISTORY) {
            recentCadences.removeAt(0)
        }

        // 이동 평균 계산
        val smoothedCadence = if (recentCadences.isNotEmpty()) {
            recentCadences.sum() / recentCadences.size
        } else {
            stepsPerMinute
        }

        Log.d("Cadence", "계산된 케이던스: $stepsPerMinute SPM")

        // 값 초기화 (5초마다)
        stepCount = 0
        lastResetTime = currentTime
        Log.d("Cadence", "케이던스 측정 초기화 완료")

        return stepsPerMinute
    }

    fun startMonitoring() {
        if (isMonitoring) {
            Log.d("Cadence", "이미 모니터링 중입니다.")
            return
        }

        Log.d("Cadence", "케이던스 모니터링 시작")
        stepDetector?.let {
            try {
                sensorManager.registerListener(
                    stepListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                isMonitoring = true
                Log.d("Cadence", "센서 리스너 등록 성공: ${it.name}")
            } catch (e: Exception) {
                Log.e("Cadence", "센서 리스너 등록 실패: ${e.message}")
            }
        } ?: Log.e("Cadence", "사용 가능한 센서가 없습니다")

        // 초기화
        stepCount = 0
        lastResetTime = System.currentTimeMillis()
        recentCadences.clear()
    }

    fun stopMonitoring() {
        if (!isMonitoring) {
            Log.d("Cadence", "모니터링 중이 아닙니다.")
            return
        }

        Log.d("Cadence", "케이던스 모니터링 중지")
        try {
            sensorManager.unregisterListener(stepListener)
            isMonitoring = false
        } catch (e: Exception) {
            Log.e("Cadence", "센서 리스너 해제 실패: ${e.message}")
        }
        recentCadences.clear()
    }

    fun pauseMonitoring() {
        if (!isMonitoring) {
            Log.d("Cadence", "모니터링 중이 아닙니다.")
            return
        }

        Log.d("Cadence", "케이던스 모니터링 일시 중지")
        try {
            sensorManager.unregisterListener(stepListener)
            isMonitoring = false
        } catch (e: Exception) {
            Log.e("Cadence", "센서 리스너 일시 중지 실패: ${e.message}")
        }
    }

    fun resumeMonitoring() {
        if (isMonitoring) {
            Log.d("Cadence", "이미 모니터링 중입니다.")
            return
        }

        Log.d("Cadence", "케이던스 모니터링 재개")
        stepDetector?.let {
            try {
                sensorManager.registerListener(
                    stepListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                isMonitoring = true
                Log.d("Cadence", "센서 리스너 재등록 성공")
            } catch (e: Exception) {
                Log.e("Cadence", "센서 리스너 재등록 실패: ${e.message}")
            }
        } ?: Log.e("Cadence", "사용 가능한 센서가 없습니다")
    }
}