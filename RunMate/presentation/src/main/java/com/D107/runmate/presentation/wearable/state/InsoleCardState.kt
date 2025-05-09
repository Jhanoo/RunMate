package com.D107.runmate.presentation.wearable.state

enum class InsoleCardState {
    CONNECTED,              // 연결됨 또는 연결 중 상태 표시 (layout_connected_insole)
    DISCONNECTED_SAVED,     // 연결 안됨 + 저장된 주소 있음 상태 표시 (layout_disconnected_insole, 텍스트/버튼 변경 가능)
    DISCONNECTED_NO_SAVED   // 연결 안됨 + 저장된 주소 없음 상태 표시 (layout_disconnected_insole, 기본 텍스트/버튼)
}