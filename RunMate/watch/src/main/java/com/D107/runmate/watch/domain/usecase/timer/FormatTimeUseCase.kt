package com.D107.runmate.watch.domain.usecase.timer

import android.annotation.SuppressLint
import javax.inject.Inject

class FormatTimeUseCase @Inject constructor() {
    @SuppressLint("DefaultLocale")
    operator fun invoke(miles: Long): String {
        val totalSeconds = miles / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}