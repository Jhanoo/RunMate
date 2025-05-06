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

    private var currentMode: AnalysisState = AnalysisState.IDLE

    // --- 상수 및 임계값 (AnalyzeGaitUseCase와 동일하게 또는 별도 관리) ---
    private val MIN_PRESSURE_THRESHOLD = 1000
    private val ACTIVE_SENSOR_COUNT_THRESHOLD = 1
    private val FOOT_STRIKE_SIMULTANEOUS_MS = 0
    private val MIN_STEP_DURATION_MS = 0//100
    private val YAW_DIFF_IN_TOEING_THRESHOLD = 8.0f
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

    // --- 실시간 분석 결과 Flow ---
    // UI 업데이트 빈도를 조절하기 위해 내부 StateFlow 사용 가능
    private val _currentAnalysisResult = MutableStateFlow(createEmptyResult())
    val currentAnalysisResult: StateFlow<GaitAnalysisResult> = _currentAnalysisResult.asStateFlow()


    fun getCalibrationDurationMs(): Long { // 또는 그냥 상수를 public으로
        return CALIBRATION_DURATION_MS
    }

    fun startCalibration(): Boolean {
        if (currentMode != AnalysisState.IDLE) {
            // 이미 캘리브레이션 중이거나 분석 중이면 시작 안 함
            println("Calibration or Analysis already in progress.")
            return false
        }
        println("Starting calibration for ${CALIBRATION_DURATION_MS}ms...")
        currentMode = AnalysisState.CALIBRATING
        isCalibrated = false
        leftYawOffset = 0f
        rightYawOffset = 0f
        calibrationDataPoints.clear()
        calibrationStartTime = System.currentTimeMillis() // 또는 첫 데이터의 timestamp 사용
        return true
    }

    private fun processCalibrationData(leftYaw: Float?, rightYaw: Float?) {
        if (currentMode != AnalysisState.CALIBRATING) return

        calibrationDataPoints.add(Pair(leftYaw, rightYaw))

        // 캘리브레이션 시간이 경과했는지 확인 (첫 데이터 수신 후 시작하도록 수정 가능)
        if (System.currentTimeMillis() - calibrationStartTime >= CALIBRATION_DURATION_MS && calibrationDataPoints.isNotEmpty()) {
            finishCalibration()
        }
    }

    private fun finishCalibration() {
        if (calibrationDataPoints.isEmpty()) {
            println("Calibration failed: No data points collected.")
            currentMode = AnalysisState.IDLE // 실패 시 IDLE로
            // 사용자에게 알림 필요
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
        currentMode = AnalysisState.IDLE // 캘리브레이션 완료 후 IDLE (분석 시작 대기)
        calibrationDataPoints.clear() // 데이터 정리
        println("Calibration finished. LeftOffset: $leftYawOffset, RightOffset: $rightYawOffset")
        // 캘리브레이션 완료 이벤트 발행하여 ViewModel에 알릴 수 있음
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
        calibrationDataPoints.clear()
        isCalibrated = false
        currentMode = AnalysisState.IDLE
        _currentAnalysisResult.value = createEmptyResult()

        // 초기 상태로 결과 Flow 업데이트
        _currentAnalysisResult.value = createEmptyResult()
    }

    fun startAnalysis(): Boolean {
        if (!isCalibrated) {
            println("Cannot start analysis: Calibration not done.")
            return false // 또는 자동 캘리브레이션 시작?
        }
        if (currentMode == AnalysisState.RUNNING) {
            println("Analysis already running.")
            return false
        }
        println("Starting gait analysis...")
        currentMode = AnalysisState.RUNNING
        analysisStartTime = 0L // 분석 데이터 기준 시간 초기화
        lastDataTimestamp = 0L
        // 누적 결과 변수들도 여기서 한번 더 초기화 (reset에서 이미 했지만, 명시적으로)
        leftFootStrikeCounts.clear()
        rightFootStrikeCounts.clear()
        leftYawSum = 0.0; rightYawSum = 0.0
        leftYawCount = 0; rightYawCount = 0
        totalLeftSteps = 0; totalRightSteps = 0
        _currentAnalysisResult.value = createEmptyResult().copy(overallGaitPattern = GaitPatternType.UNKNOWN) // 분석 시작시 결과 초기화

        return true
    }

    fun stopAnalysis() {
        if (currentMode == AnalysisState.RUNNING) {
            println("Stopping gait analysis.")
            // 최종 결과는 _currentAnalysisResult.value 에 이미 있음
        }
        currentMode = AnalysisState.IDLE
    }


    fun processData(data: CombinedInsoleData) {
        when (currentMode) {
            AnalysisState.CALIBRATING -> {
                processCalibrationData(data.left?.yaw, data.right?.yaw)
            }
            AnalysisState.RUNNING -> {
                if (analysisStartTime == 0L) {
                    analysisStartTime = data.timestamp
                }
                lastDataTimestamp = data.timestamp

                // 보정된 Yaw 값 사용
                val calibratedLeftData = data.left?.let { it.copy(yaw = it.yaw - leftYawOffset) }
                val calibratedRightData = data.right?.let { it.copy(yaw = it.yaw - rightYawOffset) }

                // println("Calibrated Data: L Yaw=${calibratedLeftData?.yaw}, R Yaw=${calibratedRightData?.yaw}")

                handleFootData(calibratedLeftData, InsoleSide.LEFT, data.timestamp)
                handleFootData(calibratedRightData, InsoleSide.RIGHT, data.timestamp)
            }
            AnalysisState.ERROR,AnalysisState.STOPPED->{

            }
            AnalysisState.IDLE -> {
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

        if (isActive && !isStance) { // 스텝 시작 (Swing -> Stance)
            isStance = true
            startTime = timestamp
            currentStepData.clear()
            insoleData?.let { currentStepData.add(it) }
        } else if (isActive && isStance) { // 스텝 진행 중 (Stance 유지)
            insoleData?.let { currentStepData.add(it) }
        } else if (!isActive && isStance) { // 스텝 종료 (Stance -> Swing)
            isStance = false
            val endTime = timestamp
            if (currentStepData.isNotEmpty() && (endTime - startTime >= MIN_STEP_DURATION_MS)) {
                // --- 스텝 완료 시점: 이 스텝 분석 및 결과 누적 ---
                analyzeAndAccumulateStep(side, currentStepData.toList(), startTime, endTime)
                // --- 분석 완료 후 현재 누적 결과 업데이트 ---
                updateCurrentAnalysisResult()
            }
            currentStepData.clear() // 분석 후 데이터 삭제
        }

        // 변경된 상태 업데이트
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

        // 1. 착지 유형 분석
        val footStrikeType = determineFootStrike(stepDataPoints)
//        println(footStrikeType)

        // 2. Yaw 평균 계산
        val validYaws = stepDataPoints.mapNotNull { it.yaw.takeIf { y -> y.isFinite() } }
        val averageYaw = if (validYaws.isNotEmpty()) validYaws.average() else null

        // 3. 결과 누적
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

        val result = GaitAnalysisResult(
            dominantLeftFootStrike = dominantLeftStrike,
            leftFootStrikeDistribution = leftFootStrikeCounts.toMap(),
            averageLeftYaw = avgLeftYaw, // 개별 Yaw 값도 여전히 유용할 수 있음
            totalLeftSteps = totalLeftSteps,
            dominantRightFootStrike = dominantRightStrike,
            rightFootStrikeDistribution = rightFootStrikeCounts.toMap(),
            averageRightYaw = avgRightYaw, // 개별 Yaw 값도 여전히 유용할 수 있음
            totalRightSteps = totalRightSteps,
            overallGaitPattern = overallPattern,
            analysisDurationMs = duration
        )
        println(result.toString())
        _currentAnalysisResult.value = result // StateFlow 업데이트
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
        // AnalyzeGaitUseCase의 determineFootStrike 로직과 동일하게 구현
        if (dataPoints.size < 2) return FootStrikeType.UNKNOWN
        val initialPoints = dataPoints.take(max(2, dataPoints.size / 5)) // 스텝 초반 20% 또는 최소 2개
        val firstTimestamp = initialPoints.first().timestamp
        val activationTimes = mutableMapOf<String, Long>()

        for (point in initialPoints) {
            if (!activationTimes.containsKey("heel") && point.heel >= MIN_PRESSURE_THRESHOLD) activationTimes["heel"] = point.timestamp
            if (!activationTimes.containsKey("forefoot") && (point.bigToe >= MIN_PRESSURE_THRESHOLD || point.smallToe >= MIN_PRESSURE_THRESHOLD)) activationTimes["forefoot"] = point.timestamp
            if (!activationTimes.containsKey("arch") && (point.archLeft >= MIN_PRESSURE_THRESHOLD || point.archRight >= MIN_PRESSURE_THRESHOLD)) activationTimes["arch"] = point.timestamp
            // 모든 부위가 활성화 시간 기록되면 일찍 멈춰도 됨 (최적화)
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
            isArchEarly -> FootStrikeType.MIDFOOT
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
            return GaitPatternType.UNKNOWN // 데이터 부족
        }

        val yawDifference = avgRightYaw - avgLeftYaw

        // println("Yaw Difference (R-L): $yawDifference, Left: $avgLeftYaw, Right: $avgRightYaw") // 디버깅 로그

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
            averageRightYaw = null, totalRightSteps = 0, overallGaitPattern = GaitPatternType.UNKNOWN
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