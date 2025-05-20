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
    private var calibrationDataPoints = mutableListOf<Pair<Float?, Float?>>()
    var isCalibrated: Boolean = false
    private val CALIBRATION_DURATION_MS = 2000L
    private var calibrationStartTime: Long = 0

    private var _currentMode = MutableStateFlow(AnalysisState.IDLE)
    val currentMode: StateFlow<AnalysisState> = _currentMode.asStateFlow()

    private val MIN_PRESSURE_THRESHOLD = 1000
    private val ACTIVE_SENSOR_COUNT_THRESHOLD = 1
    private val FOOT_STRIKE_SIMULTANEOUS_MS = 0
    private val MIN_STEP_DURATION_MS = 100 // 0이면 너무 짧은 접촉도 스텝으로 인식될 수 있음
    private val YAW_DIFF_IN_TOEING_THRESHOLD = -5.0f  // 예: -5도 미만이면 안짱
    private val YAW_DIFF_OUT_TOEING_THRESHOLD = 15.0f // 예: 15도 초과면 팔자

    private var isLeftStance = false
    private var isRightStance = false
    private var leftStartTime: Long = 0
    private var rightStartTime: Long = 0
    private var currentLeftStepData = mutableListOf<InsoleData>()
    private var currentRightStepData = mutableListOf<InsoleData>()

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
    private var lastResultTimestamp: Long = 0

    // --- 새로운 변수들 ---
    private val gaitPatternCounts = ConcurrentHashMap<GaitPatternType, Int>()
    private var lastProcessedLeftStepAvgYaw: Float? = null
    private var lastProcessedRightStepAvgYaw: Float? = null
    // --- ---

    private val _currentAnalysisResult = MutableStateFlow(createEmptyResult())
    val currentAnalysisResult: StateFlow<GaitAnalysisResult> = _currentAnalysisResult.asStateFlow()

    fun getCalibrationDurationMs(): Long = CALIBRATION_DURATION_MS

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

        if (validLeftYaws.isNotEmpty()) leftYawOffset = validLeftYaws.average().toFloat()
        if (validRightYaws.isNotEmpty()) rightYawOffset = validRightYaws.average().toFloat()

        isCalibrated = true
        _currentMode.value = AnalysisState.IDLE
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
        gaitPatternCounts.clear() // <<-- 추가
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
        leftYawOffset = 0f
        rightYawOffset = 0f
        lastProcessedLeftStepAvgYaw = null // <<-- 추가
        lastProcessedRightStepAvgYaw = null // <<-- 추가
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
        lastResultTimestamp = System.currentTimeMillis()

        leftFootStrikeCounts.clear()
        rightFootStrikeCounts.clear()
        gaitPatternCounts.clear() // <<-- 추가
        leftYawSum = 0.0; rightYawSum = 0.0
        leftYawCount = 0; rightYawCount = 0
        totalLeftSteps = 0; totalRightSteps = 0
        lastProcessedLeftStepAvgYaw = null // <<-- 추가
        lastProcessedRightStepAvgYaw = null // <<-- 추가
        _currentAnalysisResult.value = createEmptyResult().copy(
            overallGaitPattern = GaitPatternType.UNKNOWN,
            timestamp = lastResultTimestamp
        )
        return true
    }

    fun stopAnalysis() {
        if (_currentMode.value == AnalysisState.RUNNING) {
            println("Stopping gait analysis.")
            _currentAnalysisResult.value = _currentAnalysisResult.value.copy(timestamp = System.currentTimeMillis())
        }
        _currentMode.value = AnalysisState.IDLE
    }

    fun isRunning(): Boolean = _currentMode.value == AnalysisState.RUNNING
    fun isCalibrating(): Boolean = _currentMode.value == AnalysisState.CALIBRATING

    fun processData(data: CombinedInsoleData) {
        when (_currentMode.value) {
            AnalysisState.CALIBRATING -> {
                processCalibrationData(data.left?.yaw, data.right?.yaw)
            }
            AnalysisState.RUNNING -> {
                if (analysisStartTime == 0L) analysisStartTime = data.timestamp
                lastDataTimestamp = data.timestamp

                val calibratedLeftData = data.left?.let { it.copy(yaw = it.yaw - leftYawOffset) }
                val calibratedRightData = data.right?.let { it.copy(yaw = it.yaw - rightYawOffset) }

                handleFootData(calibratedLeftData, InsoleSide.LEFT, data.timestamp)
                handleFootData(calibratedRightData, InsoleSide.RIGHT, data.timestamp)
            }
            AnalysisState.ERROR, AnalysisState.STOPPED, AnalysisState.IDLE -> { /* Do nothing or log */ }
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

        if (isActive && !isStance) { // 발이 땅에 닿기 시작
            isStance = true
            startTime = timestamp
            currentStepData.clear()
            insoleData?.let { currentStepData.add(it) }
        } else if (isActive && isStance) { // 발이 계속 땅에 닿아 있음
            insoleData?.let { currentStepData.add(it) }
        } else if (!isActive && isStance) { // 발이 땅에서 떨어짐 (스텝 종료)
            isStance = false
            val endTime = timestamp
            if (currentStepData.isNotEmpty() && (endTime - startTime >= MIN_STEP_DURATION_MS)) {
                analyzeAndAccumulateStep(side, currentStepData.toList(), startTime, endTime)
                updateCurrentAnalysisResult() // 각 스텝 후 결과 업데이트
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
        val averageYawForThisStep = if (validYaws.isNotEmpty()) validYaws.average().toFloat() else null

        if (side == InsoleSide.LEFT) {
            totalLeftSteps++
            leftFootStrikeCounts[footStrikeType] = (leftFootStrikeCounts[footStrikeType] ?: 0) + 1
            if (averageYawForThisStep != null) {
                leftYawSum += averageYawForThisStep
                leftYawCount++
                // 왼발 스텝의 Yaw 값으로 패턴 판단 시도
                lastProcessedRightStepAvgYaw?.let { rightYaw ->
                    val pattern = determineInstantaneousGaitPattern(averageYawForThisStep, rightYaw)
                    gaitPatternCounts[pattern] = (gaitPatternCounts[pattern] ?: 0) + 1
                    lastProcessedRightStepAvgYaw = null // 사용했으므로 null 처리 (오른발-왼발 쌍으로 한 번만 카운트)
                } ?: run {
                    lastProcessedLeftStepAvgYaw = averageYawForThisStep // 오른발 스텝을 기다림
                }
            }
        } else { // InsoleSide.RIGHT
            totalRightSteps++
            rightFootStrikeCounts[footStrikeType] = (rightFootStrikeCounts[footStrikeType] ?: 0) + 1
            if (averageYawForThisStep != null) {
                rightYawSum += averageYawForThisStep
                rightYawCount++
                // 오른발 스텝의 Yaw 값으로 패턴 판단 시도
                lastProcessedLeftStepAvgYaw?.let { leftYaw ->
                    val pattern = determineInstantaneousGaitPattern(leftYaw, averageYawForThisStep)
                    gaitPatternCounts[pattern] = (gaitPatternCounts[pattern] ?: 0) + 1
                    lastProcessedLeftStepAvgYaw = null // 사용했으므로 null 처리 (왼발-오른발 쌍으로 한 번만 카운트)
                } ?: run {
                    lastProcessedRightStepAvgYaw = averageYawForThisStep // 왼발 스텝을 기다림
                }
            }
        }
    }

    // 각 스텝 쌍의 Yaw 값으로 즉시 패턴을 결정하는 함수
    private fun determineInstantaneousGaitPattern(leftYaw: Float, rightYaw: Float): GaitPatternType {
        // 주의: leftYaw와 rightYaw는 이미 offset이 적용된 값이어야 합니다.
        // analyzeAndAccumulateStep에서 averageYawForThisStep을 계산할 때 사용된 yaw 값들은
        // processData -> handleFootData를 거치면서 이미 offset이 적용된 calibratedData의 yaw입니다.
        val yawDifference = rightYaw - leftYaw // 오른발 Yaw - 왼발 Yaw
        return when {
            yawDifference < YAW_DIFF_IN_TOEING_THRESHOLD -> GaitPatternType.IN_TOEING
            yawDifference > YAW_DIFF_OUT_TOEING_THRESHOLD -> GaitPatternType.OUT_TOEING
            else -> GaitPatternType.NEUTRAL
        }
    }


    private fun updateCurrentAnalysisResult() {
        val avgLeftYaw = if (leftYawCount > 0) (leftYawSum / leftYawCount).toFloat() else null
        val avgRightYaw = if (rightYawCount > 0) (rightYawSum / rightYawCount).toFloat() else null

        val dominantLeftStrike = findDominantStrike(leftFootStrikeCounts)
        val dominantRightStrike = findDominantStrike(rightFootStrikeCounts)

        // 수정된 부분: 카운트 기반으로 전체 걸음 패턴 결정
        val overallPattern = determineOverallGaitPatternFromCounts(gaitPatternCounts)

        val duration = if (analysisStartTime > 0 && lastDataTimestamp > analysisStartTime) {
            lastDataTimestamp - analysisStartTime
        } else {
            0L
        }
        lastResultTimestamp = System.currentTimeMillis()

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
            gaitPatternDistribution = gaitPatternCounts.toMap(),
            analysisDurationMs = duration,
            timestamp = lastResultTimestamp
        )
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
        // val firstTimestamp = initialPoints.first().timestamp // 사용되지 않음
        val activationTimes = mutableMapOf<String, Long>()

        for (point in initialPoints) {
            if (!activationTimes.containsKey("heel") && point.heel >= MIN_PRESSURE_THRESHOLD) activationTimes["heel"] = point.timestamp
            if (!activationTimes.containsKey("forefoot") && (point.bigToe >= MIN_PRESSURE_THRESHOLD || point.smallToe >= MIN_PRESSURE_THRESHOLD)) activationTimes["forefoot"] = point.timestamp
            if (!activationTimes.containsKey("arch") && (point.archLeft >= MIN_PRESSURE_THRESHOLD || point.archRight >= MIN_PRESSURE_THRESHOLD)) activationTimes["arch"] = point.timestamp
            if (activationTimes.containsKey("heel") && activationTimes.containsKey("forefoot")) break // 아치보다 앞/뒤꿈치 우선
        }

        if (activationTimes.isEmpty()) return FootStrikeType.UNKNOWN

        val firstActivationTime = activationTimes.minOfOrNull { it.value } ?: Long.MAX_VALUE
        val heelActivationTime = activationTimes.getOrDefault("heel", Long.MAX_VALUE)
        val forefootActivationTime = activationTimes.getOrDefault("forefoot", Long.MAX_VALUE)
        // val archActivationTime = activationTimes.getOrDefault("arch", Long.MAX_VALUE) // 현재 로직에서 아치 단독 판단은 없음

        val isHeelEarly = heelActivationTime <= firstActivationTime + FOOT_STRIKE_SIMULTANEOUS_MS
        val isForefootEarly = forefootActivationTime <= firstActivationTime + FOOT_STRIKE_SIMULTANEOUS_MS
        // val isArchEarly = archActivationTime <= firstActivationTime + FOOT_STRIKE_SIMULTANEOUS_MS // 현재 로직에서 아치 단독 판단은 없음


        return when {
            isHeelEarly && isForefootEarly -> FootStrikeType.MIDFOOT // 둘 다 거의 동시에 들어오면 MIDFOOT
            isHeelEarly -> FootStrikeType.REARFOOT
            isForefootEarly -> FootStrikeType.FOREFOOT
            // isArchEarly -> FootStrikeType.MIDFOOT // 아치 우선 순위를 두려면 이 조건 추가 및 순서 조정 필요
            else -> FootStrikeType.UNKNOWN
        }
    }

    private fun findDominantStrike(strikeCounts: Map<FootStrikeType, Int>): FootStrikeType {
        val dominant = strikeCounts.filterKeys { it != FootStrikeType.UNKNOWN }
            .maxByOrNull { it.value }?.key
        return dominant ?: FootStrikeType.UNKNOWN
    }

    // 수정된 함수: Yaw 차이 평균 대신 카운트 기반으로 전체 패턴 결정
    private fun determineOverallGaitPatternFromCounts(counts: Map<GaitPatternType, Int>): GaitPatternType {
        if (counts.isEmpty()) return GaitPatternType.UNKNOWN
        // UNKNOWN을 제외하고 가장 빈번한 패턴을 찾음
        return counts.filterKeys { it != GaitPatternType.UNKNOWN }
            .maxByOrNull { it.value }?.key ?: GaitPatternType.NEUTRAL // 기본값으로 NEUTRAL 또는 UNKNOWN 선택
    }


    private fun createEmptyResult(): GaitAnalysisResult {
        return GaitAnalysisResult(
            dominantLeftFootStrike = FootStrikeType.UNKNOWN, leftFootStrikeDistribution = emptyMap(),
            averageLeftYaw = null, totalLeftSteps = 0,
            dominantRightFootStrike = FootStrikeType.UNKNOWN, rightFootStrikeDistribution = emptyMap(),
            averageRightYaw = null, totalRightSteps = 0,
            overallGaitPattern = GaitPatternType.UNKNOWN,
            gaitPatternDistribution = emptyMap(), // <<-- 추가된 필드 초기화
            timestamp = System.currentTimeMillis()
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
