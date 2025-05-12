package com.D107.runmate.presentation.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

    fun dateformatYMDHMFromInt(year: Int, month: Int, day: Int): String {
//        val format = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.KOREA)
//        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
//        return format.format(time)
        return "${year}.${month}.${day}."
    }

    //날짜 포맷 출력
    fun dateformatYMDHM(time: Date): String {
        val format = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.KOREA)
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return format.format(time)
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
//            val file = context.assets.open("test_3.gpx") // test용
            //            file
            val file = File(context.filesDir, "running_tracking.gpx") // 실제로 작성한 파일
            FileInputStream(file)

        } catch (e: Exception) {
            Timber.e(e, "GPX 파일 읽기 실패")
            null
        }
    }
}

sealed class ToastType {
    object DEFAULT : ToastType()
    object ERROR : ToastType()
}