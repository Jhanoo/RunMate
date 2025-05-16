package com.D107.runmate.presentation.user.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.DialogDatePickerBinding
import com.D107.runmate.presentation.utils.CommonUtils
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
class CustomDatePicker(
    private val initialYear: Int = 0,
    private val initialMonth: Int = 0,
    private val initialDay: Int = 0,
    private val onDateSelected: (year: Int, month: Int, day: Int, formattedDate: String) -> Unit
) : DialogFragment() {
    private var mContext: Context? = null
    private lateinit var binding: DialogDatePickerBinding

    private var selectedYear = initialYear
    private var selectedMonth = initialMonth
    private var selectedDay = initialDay

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDatePickerBinding.inflate(inflater, container, false)
        isCancelable = true
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val point = CommonUtils.getWindowSize(mContext!!)
        val deviceWidth = point.x
        params?.width = (deviceWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams

        // 다이얼로그 배경을 투명하게 설정
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPickers()

        // 설정 버튼 클릭 리스너
        binding.confirmButton.setOnClickListener {
            selectedYear = binding.yearPicker.value
            selectedMonth = binding.monthPicker.value
            selectedDay = binding.dayPicker.value

            // 선택한 날짜를 포맷팅
            val formattedMonth = String.format("%02d", selectedMonth)
            val formattedDay = String.format("%02d", selectedDay)
            val formattedDate = "$selectedYear $formattedMonth $formattedDay"

            // 콜백 호출
            onDateSelected(selectedYear, selectedMonth, selectedDay, formattedDate)

            dialog?.dismiss()
        }

        // 닫기 버튼 클릭 리스너
        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun setupPickers() {
        // 현재 날짜 가져오기
        val calendar = Calendar.getInstance()
        val currentYear = java.time.LocalDate.now().year

        // 연도 설정 (1950년부터 현재 연도까지)
        binding.yearPicker.apply {
            minValue = 1950
            maxValue = currentYear
            value = if (selectedYear > 0) selectedYear else 2000 // 기본값 2000년
            wrapSelectorWheel = false
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        // 월 설정 (1~12월)
        binding.monthPicker.apply {
            minValue = 1
            maxValue = 12
            value = if (selectedMonth > 0) selectedMonth else 1 // 기본값 1월
            wrapSelectorWheel = false
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

            // 포맷터 설정하여 앞에 0 붙이기
            setFormatter { value ->
                String.format("%02d", value)
            }
        }

        // 일 설정 (1~31일)
        updateDayPicker(binding.yearPicker.value, binding.monthPicker.value)

        // 연도나 월이 변경되면 일(day) 최대값 업데이트
        binding.yearPicker.setOnValueChangedListener { _, _, newVal ->
            updateDayPicker(newVal, binding.monthPicker.value)
        }

        binding.monthPicker.setOnValueChangedListener { _, _, newVal ->
            updateDayPicker(binding.yearPicker.value, newVal)
        }
    }

    private fun updateDayPicker(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // 해당 년월의 1일로 설정
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        binding.dayPicker.apply {
            minValue = 1
            maxValue = maxDay
            wrapSelectorWheel = false
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

            // 현재 선택된 일이 새 최대값보다 크면 최대값으로 조정
            if (selectedDay > maxDay) {
                selectedDay = maxDay
            }

            value = if (selectedDay > 0) minOf(selectedDay, maxDay) else 1 // 기본값 1일

            // 포맷터 설정하여 앞에 0 붙이기
            setFormatter { value ->
                String.format("%02d", value)
            }
        }
    }
}