package com.D107.runmate.domain.model.running

sealed class RunningTrackingState {
    object Initial : RunningTrackingState()  // 시작 전
    data class Active(val startTime: Long) : RunningTrackingState()  // 추적 중
    data class Paused(val elapsedTime: Long) : RunningTrackingState()  // 일시정지
}

sealed class UserLocationState {
    object Initial: UserLocationState()
    data class Exist(val locations: List<LocationModel>): UserLocationState()
}

sealed class RunningRecordState {
    object Initial: RunningRecordState()
    data class Exist(val runningRecords: List<PersonalRunningInfo>): RunningRecordState()
}

enum class TrackingStatus { INITIAL, RUNNING, PAUSED, STOPPED }