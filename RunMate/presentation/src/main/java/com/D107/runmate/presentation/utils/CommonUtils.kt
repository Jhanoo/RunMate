package com.D107.runmate.presentation.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import dagger.hilt.android.internal.managers.ViewComponentManager
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonUtils {
    //천단위 콤마
    fun makeComma(num: Int): String {
        val comma = DecimalFormat("#,###")
        return comma.format(num)
    }

    fun makeCommaDecimal(num: BigDecimal): String {
        val comma = DecimalFormat("#,###")
        return comma.format(num)
    }

    fun dateformatMMdd(dateString: String): String {
        val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatterOutput = DateTimeFormatter.ofPattern("MM.dd")
        val dateTime = LocalDateTime.parse(dateString, formatterInput)
        return dateTime.format(formatterOutput)
    }

    //날짜 포맷 출력
    fun dateformatYMDHM(time: Date): String {
        val format = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.KOREA)
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return format.format(time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatIsoDateToCustom(isoDateString: String): String {
        val offsetDateTime = OffsetDateTime.parse(isoDateString)
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.getDefault())

        return offsetDateTime.format(outputFormatter)
    }

    fun dateformatYMD(time: Date): String {
        val format = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return format.format(time)
    }

    fun formatLongToDate(longDate: Long): String {
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())  // 원하는 날짜 형식 지정
        return format.format(Date(longDate))  // Long 값을 Date 객체로 변환 후 포맷 적용
    }

    fun formatNumber(number: String): String {
        return try {
            val num = number.toLong()
            NumberFormat.getNumberInstance(Locale.KOREA).format(num)
        } catch (e: Exception) {
            number
        }
    }

    fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    fun floorTenThousandDecimal(prev: BigDecimal, current: BigDecimal): BigDecimal {
        return prev.subtract(current)
            .divide(BigDecimal(10000), 0, RoundingMode.DOWN)
    }

    fun View.expandTouchArea(extraPadding: Int = 30) {
        val parentView = this.parent as? ViewGroup ?: return

        parentView.post {
            val rect = Rect()
            this.getHitRect(rect)
            rect.top -= extraPadding
            rect.bottom += extraPadding
            rect.left -= extraPadding
            rect.right += extraPadding
            parentView.touchDelegate = TouchDelegate(rect, this)
        }
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
        ).toInt()
    }

    fun getActivityContext(context: Context): Context {
        return if (context is ViewComponentManager.FragmentContextWrapper) {
            context.baseContext
        } else {
            context
        }
    }

    fun getWindowSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        return size
    }

    fun getGpxInputStream(context: Context): InputStream? {
        return try {
            val file = File(context.filesDir, "running_tracking.gpx") // 실제로 작성한 파일
            FileInputStream(file)
        } catch (e: Exception) {
            Timber.e(e, "GPX 파일 읽기 실패")
            null
        }
    }

    fun convertDateTime(input: String): String {
        // 입력 문자열을 LocalDateTime으로 파싱
        val localDateTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        // Asia/Seoul 타임존으로 ZonedDateTime 생성
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Seoul"))
        // ISO_OFFSET_DATE_TIME 포맷으로 출력
        return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun formatSecondsToHMS(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun formatSecondsToMS(seconds: Int): String {
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    fun getSecondsBetween(time1: String, time2: String): Long {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime1 = LocalDateTime.parse(time1, formatter)
        val dateTime2 = LocalDateTime.parse(time2, formatter)
        return Duration.between(dateTime1, dateTime2).seconds
    }
}

sealed class ToastType {
    object DEFAULT : ToastType()
    object ERROR : ToastType()
}