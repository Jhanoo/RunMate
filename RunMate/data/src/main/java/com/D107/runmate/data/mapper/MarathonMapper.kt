package com.D107.runmate.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.D107.runmate.data.remote.response.manager.MarathonResponse
import com.D107.runmate.domain.model.manager.MarathonInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
object MarathonMapper : DataMapper<MarathonResponse, MarathonInfo> {
    override fun MarathonResponse.toDomainModel(): MarathonInfo {
        val date = java.time.LocalDate.parse(this.date.substring(0, 10))
        val month = date.monthValue
        val day = date.dayOfMonth
        val formattedDate = "$month/$day"

        val dayOfWeek = when(date.dayOfWeek) {
            java.time.DayOfWeek.SUNDAY -> "일"
            java.time.DayOfWeek.MONDAY -> "월"
            java.time.DayOfWeek.TUESDAY -> "화"
            java.time.DayOfWeek.WEDNESDAY -> "수"
            java.time.DayOfWeek.THURSDAY -> "목"
            java.time.DayOfWeek.FRIDAY -> "금"
            java.time.DayOfWeek.SATURDAY -> "토"
        }

        return MarathonInfo(
            id = marathonId,
            title = name,
            date = formattedDate,
            dayOfWeek = dayOfWeek,
            location = location,
            distance = distance
        )
    }
}