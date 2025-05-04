package com.D107.runmate.domain.model.Insole

import com.D107.runmate.domain.model.base.BaseModel

data class InsoleData(
    val bigToe: Int,//엄지 발가락 밑
    val smallToe: Int,//새끼 발가락 밑
    val heel: Int,//발 뒷꿈치
    val archLeft: Int,//발 아치 왼쪽
    val archRight: Int,//발 아치 오른쪽
    val yaw: Float,
    val pitch: Float,
    val roll: Float,
    val timestamp: Long = System.currentTimeMillis()
):BaseModel