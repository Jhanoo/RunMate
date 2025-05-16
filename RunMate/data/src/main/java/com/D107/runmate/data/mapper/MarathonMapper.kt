package com.D107.runmate.data.mapper

import com.D107.runmate.data.remote.response.manager.MarathonResponse
import com.D107.runmate.domain.model.manager.MarathonInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MarathonMapper : DataMapper<MarathonResponse, MarathonInfo> {
    override fun MarathonResponse.toDomainModel(): MarathonInfo {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormatter.parse(this.date)
        val calendar = Calendar.getInstance().apply { time = date!! }

        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedDate = "$month/$day"

        val dayOfWeek = when(calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
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