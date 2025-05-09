package com.D107.runmate.watch.domain.usecase.pace

import android.annotation.SuppressLint
import android.util.Log
import javax.inject.Inject

class CalculatePaceUseCase @Inject constructor() {
    @SuppressLint("DefaultLocale")
    operator fun invoke(distanceKm: Double, timeSeconds: Long): String {
        if(distanceKm <= 0) return "--'--\""

        // 최소 유효 거리 체크 (10m 미만은 무시)
        if(distanceKm < 0.01) {
            Log.d("pace", "거리가 너무 작음: $distanceKm km, 페이스 계산 보류")
            return "--'--\""
        }

        val secondsPerKm = timeSeconds / distanceKm

        // 페이스 최대값 제한 (30분/km)
        if (secondsPerKm > 1800) { // 30분 = 1800초
            Log.d("pace", "페이스 값 최대치 초과: ${secondsPerKm/60}분/km, 최대값으로 표시")
            return "30'00\""
        }

        val minutes = (secondsPerKm / 60).toInt()
        val seconds = (secondsPerKm % 60).toInt()

        val result = String.format("%d'%02d\"", minutes, seconds)
        Log.d("pace", "페이스 계산: 거리=$distanceKm km, 시간=$timeSeconds 초, 결과=$result")
        return result
    }
}