package com.D107.runmate.presentation.components

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.NumberPicker
import com.D107.runmate.presentation.R
import java.util.Calendar

class CustomDatePicker(
    private val context: Context,
    private val initialYear: Int = 0,
    private val initialMonth: Int = 0,
    private val initialDay: Int = 0,
    private val onDateSelected: (year: Int, month: Int, day: Int, formattedDate: String) -> Unit
) {
    private var selectedYear = initialYear
    private var selectedMonth = initialMonth
    private var selectedDay = initialDay

    @SuppressLint("DefaultLocale")
    fun show() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_date_picker)

        // 다이얼로그 배경을 투명하게 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 크기 설정
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        // NumberPicker 초기화
        val yearPicker = dialog.findViewById<NumberPicker>(R.id.yearPicker)
        val monthPicker = dialog.findViewById<NumberPicker>(R.id.monthPicker)
        val dayPicker = dialog.findViewById<NumberPicker>(R.id.dayPicker)

        // 현재 날짜 가져오기
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // 연도 설정 (1950년부터 현재 연도까지)
        yearPicker.minValue = 1950
        yearPicker.maxValue = currentYear
        yearPicker.value = if (selectedYear > 0) selectedYear else 2000 // 기본값 2000년

        // 월 설정 (1~12월)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = if (selectedMonth > 0) selectedMonth else 1 // 기본값 1월

        // 포맷터 설정하여 앞에 0 붙이기
        monthPicker.setFormatter { value ->
            String.format("%02d", value)
        }

        // 일 설정 (1~31일)
        updateDayPicker(dayPicker, yearPicker.value, monthPicker.value)

        // 연도나 월이 변경되면 일(day) 최대값 업데이트
        yearPicker.setOnValueChangedListener { _, _, newVal ->
            updateDayPicker(dayPicker, newVal, monthPicker.value)
        }

        monthPicker.setOnValueChangedListener { _, _, newVal ->
            updateDayPicker(dayPicker, yearPicker.value, newVal)
        }

        // 설정 버튼 클릭 리스너
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            selectedYear = yearPicker.value
            selectedMonth = monthPicker.value
            selectedDay = dayPicker.value

            // 선택한 날짜를 포맷팅
            val formattedMonth = String.format("%02d", selectedMonth)
            val formattedDay = String.format("%02d", selectedDay)
            val formattedDate = "$selectedYear $formattedMonth $formattedDay"

            // 콜백 호출
            onDateSelected(selectedYear, selectedMonth, selectedDay, formattedDate)

            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun updateDayPicker(dayPicker: NumberPicker?, year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // 해당 년월의 1일로 설정
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        dayPicker?.minValue = 1
        dayPicker?.maxValue = maxDay

        // 현재 선택된 일이 새 최대값보다 크면 최대값으로 조정
        if (selectedDay > maxDay) {
            selectedDay = maxDay
        }

        dayPicker?.value = if (selectedDay > 0)
            minOf(selectedDay, maxDay) else 1 // 기본값 1일

        // 포맷터 설정하여 앞에 0 붙이기
        dayPicker?.setFormatter { value ->
            String.format("%02d", value)
        }
    }
}