package com.D107.runmate.domain.util

import com.D107.runmate.domain.model.smartinsole.CombinedInsoleData
import com.D107.runmate.domain.model.smartinsole.FootStrikeType
import com.D107.runmate.domain.model.smartinsole.GaitAnalysisResult
import com.D107.runmate.domain.model.smartinsole.GaitPatternType
import com.D107.runmate.domain.model.smartinsole.InsoleData
import com.D107.runmate.domain.model.smartinsole.InsoleSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.max

class GaitAnalyzerUtil @Inject constructor() {


    private var leftYawOffset: Float = 0f
    private var rightYawOffset: Float = 0f
    private var calibrationDataPoints = mutableListOf<Pair<Float?, Float?>>() // (leftYaw, rightYaw)
    var isCalibrated: Boolean = false
    private val CALIBRATION_DURATION_MS = 2000L // 예: 2초 동안 캘리브레이션
    private var calibrationStartTime: Long = 0

    private var _currentMode = MutableStateFlow(AnalysisState.IDLE) // ViewModel에서 사용하던 AnalysisProcessState와 유사한 역할
    val currentMode: StateFlow<AnalysisState> = _currentMode.asStateFlow()


    // --- 상수 및 임계값 (AnalyzeGaitUseCase와 동일하게 또는 별도 관리) ---
    private val MIN_PRESSURE_THRESHOLD = 1000
    private val ACTIVE_SENSOR_COUNT_THRESHOLD = 1
    private val FOOT_STRIKE_SIMULTANEOUS_MS = 0
    private val MIN_STEP_DURATION_MS = 0//100
    private val YAW_DIFF_IN_TOEING_THRESHOLD = -5.0f
    private val YAW_DIFF_OUT_TOEING_THRESHOLD = 15.0f

    // --- 실시간 상태 관리 변수 ---
    private var isLeftStance = false
    private var isRightStance = false
    private var leftStartTime: Long = 0
    private var rightStartTime: Long = 0
    private var currentLeftStepData = mutableListOf<InsoleData>()
    private var currentRightStepData = mutableListOf<InsoleData>()

    // --- 누적 결과 저장 변수 ---
    private val leftFootStrikeCounts = ConcurrentHashMap<FootStrikeType, Int>()
    private val rightFootStrikeCounts = ConcurrentHashMap<FootStrikeType, Int>()
    private var leftYawSum: Double = 0.0
    private var rightYawSum: Double = 0.0
    private var leftYawCount: Int = 0
    private var rightYawCount: Int = 0
    private var totalLeftSteps: Int = 0
    private var totalRightSteps: Int = 0
    private var analysisStartTime: Long = 0
    private var lastDataTimestamp: Long = 0
    private var lastResultTimestamp: Long = 0 // 결과 생성 시점 타임스탬프

    // --- 실시간 분석 결과 Flow ---
    private val _currentAnalysisResult = MutableStateFlow(createEmptyResult())
    val currentAnalysisResult: StateFlow<GaitAnalysisResult> = _currentAnalysisResult.asStateFlow()


    fun getCalibrationDurationMs(): Long {
        return CALIBRATION_DURATION_MS
    }

    fun startCalibration(): Boolean {
        if (_currentMode.value != AnalysisState.IDLE) {
            println("Calibration or Analysis already in progress.")
            return false
        }
        println("Starting calibration for ${CALIBRATION_DURATION_MS}ms...")
        _currentMode.value = AnalysisState.CALIBRATING
        isCalibrated = false
        leftYawOffset = 0f
        rightYawOffset = 0f
        calibrationDataPoints.clear()
        calibrationStartTime = System.currentTimeMillis()
        return true
    }

    private fun processCalibrationData(leftYaw: Float?, rightYaw: Float?) {
        if (_currentMode.value != AnalysisState.CALIBRATING) return

        calibrationDataPoints.add(Pair(leftYaw, rightYaw))

        if (System.currentTimeMillis() - calibrationStartTime >= CALIBRATION_DURATION_MS && calibrationDataPoints.isNotEmpty()) {
            finishCalibration()
        }
    }

    private fun finishCalibration() {
        if (calibrationDataPoints.isEmpty()) {
            println("Calibration failed: No data points collected.")
            _currentMode.value = AnalysisState.IDLE
            return
        }

        val validLeftYaws = calibrationDataPoints.mapNotNull { it.first }
        val validRightYaws = calibrationDataPoints.mapNotNull { it.second }

        if (validLeftYaws.isNotEmpty()) {
            leftYawOffset = validLeftYaws.average().toFloat()
        }
        if (validRightYaws.isNotEmpty()) {
            rightYawOffset = validRightYaws.average().toFloat()
        }
        isCalibrated = true
        _currentMode.value = AnalysisState.IDLE // 또는 READY_TO_ANALYZE 같은 상태
        calibrationDataPoints.clear()
        println("Calibration finished. LeftOffset: $leftYawOffset, RightOffset: $rightYawOffset")
    }


    fun reset() {
        isLeftStance = false
        isRightStance = false
        leftStartTime = 0
        rightStartTime = 0
        currentLeftStepData.clear()
        currentRightStepData.clear()

        leftFootStrikeCounts.clear()
        rightFootStrikeCounts.clear()
        leftYawSum = 0.0
        rightYawSum = 0.0
        leftYawCount = 0
        rightYawCount = 0
        totalLeftSteps = 0
        totalRightSteps = 0
        analysisStartTime = 0
        lastDataTimestamp = 0
        lastResultTimestamp = 0
        calibrationDataPoints.clear()
        isCalibrated = false
        leftYawOffset = 0f // 리셋 시 오프셋도 초기화
        rightYawOffset = 0f
        _currentMode.value = AnalysisState.IDLE
        _currentAnalysisResult.value = createEmptyResult()
    }

    fun startAnalysis(): Boolean {
        if (!isCalibrated) {
            println("Cannot start analysis: Calibration not done.")
            return false
        }
        if (_currentMode.value == AnalysisState.RUNNING) {
            println("Analysis already running.")
            return false
        }
        println("Starting gait analysis...")
        _currentMode.value = AnalysisState.RUNNING
        analysisStartTime = 0L
        lastDataTimestamp = 0L
        lastResultTimestamp = System.currentTimeMillis() // 분석 시작 시 타임스탬프 설정

        leftFootStrikeCounts.clear()
        rightFootStrikeCounts.clear()
        leftYawSum = 0.0; rightYawSum = 0.0
        leftYawCount = 0; rightYawCount = 0
        totalLeftSteps = 0; totalRightSteps = 0
        _currentAnalysisResult.value = createEmptyResult().copy(
            overallGaitPattern = GaitPatternType.UNKNOWN,
            timestamp = lastResultTimestamp // 생성 시점 기록
        )
        return true
    }

    fun stopAnalysis() {
        if (_currentMode.value == AnalysisState.RUNNING) {
            println("Stopping gait analysis.")
            // 최종 결과 업데이트 시 타임스탬프 갱신
            _currentAnalysisResult.value = _currentAnalysisResult.value.copy(timestamp = System.currentTimeMillis())
        }
        _currentMode.value = AnalysisState.IDLE // 또는 STOPPED
    }

    fun isRunning(): Boolean = _currentMode.value == AnalysisState.RUNNING
    fun isCalibrating(): Boolean = _currentMode.value == AnalysisState.CALIBRATING


    fun processData(data: CombinedInsoleData) {
        when (_currentMode.value) {
            AnalysisState.CALIBRATING -> {
                processCalibrationData(data.left?.yaw, data.right?.yaw)
            }
            AnalysisState.RUNNING -> {
                if (analysisStartTime == 0L) {
                    analysisStartTime = data.timestamp
                }
                lastDataTimestamp = data.timestamp

                val calibratedLeftData = data.left?.let { it.copy(yaw = it.yaw - leftYawOffset) }
                val calibratedRightData = data.right?.let { it.copy(yaw = it.yaw - rightYawOffset) }

                handleFootData(calibratedLeftData, InsoleSide.LEFT, data.timestamp)
                handleFootData(calibratedRightData, InsoleSide.RIGHT, data.timestamp)
            }
            AnalysisState.ERROR,AnalysisState.STOPPED, AnalysisState.IDLE -> {
                // Do nothing or log
            }
        }
    }


    private fun handleFootData(insoleData: InsoleData?, side: InsoleSide, timestamp: Long) {
        val isActive = isFootActive(insoleData)
        var isStance: Boolean
        var startTime: Long
        var currentStepData: MutableList<InsoleData>


        if (side == InsoleSide.LEFT) {
            isStance = isLeftStance
            startTime = leftStartTime
            currentStepData = currentLeftStepData
        } else {
            isStance = isRightStance
            startTime = rightStartTime
            currentStepData = currentRightStepData
        }

        if (isActive && !isStance) {
            isStance = true
            startTime = timestamp
            currentStepData.clear()
            insoleData?.let { currentStepData.add(it) }
        } else if (isActive && isStance) {
            insoleData?.let { currentStepData.add(it) }
        } else if (!isActive && isStance) {
            isStance = false
            val endTime = timestamp
            if (currentStepData.isNotEmpty() && (endTime - startTime >= MIN_STEP_DURATION_MS)) {
                analyzeAndAccumulateStep(side, currentStepData.toList(), startTime, endTime)
                updateCurrentAnalysisResult()
            }
            currentStepData.clear()
        }

        if (side == InsoleSide.LEFT) {
            isLeftStance = isStance
            leftStartTime = startTime
        } else {
            isRightStance = isStance
            rightStartTime = startTime
        }
    }


    private fun analyzeAndAccumulateStep(side: InsoleSide, stepDataPoints: List<InsoleData>, startTime: Long, endTime: Long) {
        if (stepDataPoints.isEmpty()) return

        val footStrikeType = determineFootStrike(stepDataPoints)
        val validYaws = stepDataPoints.mapNotNull { it.yaw.takeIf { y -> y.isFinite() } }
        val averageYaw = if (validYaws.isNotEmpty()) validYaws.average() else null

        if (side == InsoleSide.LEFT) {
            totalLeftSteps++
            leftFootStrikeCounts[footStrikeType] = (leftFootStrikeCounts[footStrikeType] ?: 0) + 1
            if (averageYaw != null) {
                leftYawSum += averageYaw
                leftYawCount++
            }
        } else {
            totalRightSteps++
            rightFootStrikeCounts[footStrikeType] = (rightFootStrikeCounts[footStrikeType] ?: 0) + 1
            if (averageYaw != null) {
                rightYawSum += averageYaw
                rightYawCount++
            }
        }
    }


    private fun updateCurrentAnalysisResult() {
        val avgLeftYaw = if (leftYawCount > 0) (leftYawSum / leftYawCount).toFloat() else null
        val avgRightYaw = if (rightYawCount > 0) (rightYawSum / rightYawCount).toFloat() else null

        val dominantLeftStrike = findDominantStrike(leftFootStrikeCounts)
        val dominantRightStrike = findDominantStrike(rightFootStrikeCounts)

        val overallPattern = determineOverallGaitPattern(avgLeftYaw, avgRightYaw)

        val duration = if (analysisStartTime > 0 && lastDataTimestamp > analysisStartTime) {
            lastDataTimestamp - analysisStartTime
        } else {
            0L
        }
        lastResultTimestamp = System.currentTimeMillis() // 결과 업데이트 시점 갱신

        val result = GaitAnalysisResult(
            dominantLeftFootStrike = dominantLeftStrike,
            leftFootStrikeDistribution = leftFootStrikeCounts.toMap(),
            averageLeftYaw = avgLeftYaw,
            totalLeftSteps = totalLeftSteps,
            dominantRightFootStrike = dominantRightStrike,
            rightFootStrikeDistribution = rightFootStrikeCounts.toMap(),
            averageRightYaw = avgRightYaw,
            totalRightSteps = totalRightSteps,
            overallGaitPattern = overallPattern,
            analysisDurationMs = duration,
            timestamp = lastResultTimestamp // 생성 시점 기록
        )
        // println(result.toString()) //Timber로 대체 권장
        _currentAnalysisResult.value = result
    }


    private fun isFootActive(insoleData: InsoleData?): Boolean {
        if (insoleData == null) return false
        val activeSensorCount = listOf(
            insoleData.bigToe, insoleData.smallToe, insoleData.heel,
            insoleData.archLeft, insoleData.archRight
        ).count { it >= MIN_PRESSURE_THRESHOLD }
        return activeSensorCount >= ACTIVE_SENSOR_COUNT_THRESHOLD
    }

    private fun determineFootStrike(dataPoints: List<InsoleData>): FootStrikeType {
        if (dataPoints.size < 2) return FootStrikeType.UNKNOWN
        val initialPoints = dataPoints.take(max(2, dataPoints.size / 5))
        val firstTimestamp = initialPoints.first().timestamp
        val activationTimes = mutableMapOf<String, Long>()

        for (point in initialPoints) {
            if (!activationTimes.containsKey("heel") && point.heel >= MIN_PRESSURE_THRESHOLD) activationTimes["heel"] = point.timestamp
            if (!activationTimes.containsKey("forefoot") && (point.bigToe >= MIN_PRESSURE_THRESHOLD || point.smallToe >= MIN_PRESSURE_THRESHOLD)) activationTimes["forefoot"] = point.timestamp
            if (!activationTimes.containsKey("arch") && (point.archLeft >= MIN_PRESSURE_THRESHOLD || point.archRight >= MIN_PRESSURE_THRESHOLD)) activationTimes["arch"] = point.timestamp
            if (activationTimes.size == 3) break
        }

        if (activationTimes.isEmpty()) return FootStrikeType.UNKNOWN
        val firstActivationTime = activationTimes.minOf { it.value }
        val heelActivationTime = activationTimes.getOrDefault("heel", Long.MAX_VALUE)
        val forefootActivationTime = activationTimes.getOrDefault("forefoot", Long.MAX_VALUE)
        val archActivationTime = activationTimes.getOrDefault("arch", Long.MAX_VALUE)

        val isHeelEarly = heelActivationTime <= firstActivationTime + FOOT_STRIKE_SIMULTANEOUS_MS
        val isForefootEarly = forefootActivationTime <= firstActivationTime + FOOT_STRIKE_SIMULTANEOUS_MS
        val isArchEarly = archActivationTime <= firstActivationTime + FOOT_STRIKE_SIMULTANEOUS_MS

        return when {
            isHeelEarly && !isForefootEarly && !isArchEarly -> FootStrikeType.REARFOOT
            isForefootEarly && !isHeelEarly && !isArchEarly -> FootStrikeType.FOREFOOT
            isHeelEarly && isForefootEarly -> FootStrikeType.MIDFOOT
            isArchEarly -> FootStrikeType.MIDFOOT // 아치 우선 순위 적용 시
            else -> FootStrikeType.UNKNOWN
        }
    }

    private fun findDominantStrike(strikeCounts: Map<FootStrikeType, Int>): FootStrikeType {
        val dominant = strikeCounts.filterKeys { it != FootStrikeType.UNKNOWN }
            .maxByOrNull { it.value }?.key
        return dominant ?: FootStrikeType.UNKNOWN
    }

    private fun determineOverallGaitPattern(avgLeftYaw: Float?, avgRightYaw: Float?): GaitPatternType {
        if (avgLeftYaw == null || avgRightYaw == null) {
            return GaitPatternType.UNKNOWN
        }
        val yawDifference = avgRightYaw - avgLeftYaw
        return when {
            yawDifference < YAW_DIFF_IN_TOEING_THRESHOLD -> GaitPatternType.IN_TOEING
            yawDifference > YAW_DIFF_OUT_TOEING_THRESHOLD -> GaitPatternType.OUT_TOEING
            else -> GaitPatternType.NEUTRAL
        }
    }

    private fun createEmptyResult(): GaitAnalysisResult {
        return GaitAnalysisResult(
            dominantLeftFootStrike = FootStrikeType.UNKNOWN, leftFootStrikeDistribution = emptyMap(),
            averageLeftYaw = null,  totalLeftSteps = 0,
            dominantRightFootStrike = FootStrikeType.UNKNOWN, rightFootStrikeDistribution = emptyMap(),
            averageRightYaw = null, totalRightSteps = 0, overallGaitPattern = GaitPatternType.UNKNOWN,
            timestamp = System.currentTimeMillis() // 생성 시점 기록
        )
    }
}

enum class AnalysisState {
    IDLE,
    CALIBRATING,
    RUNNING,
    STOPPED,
    ERROR
}
