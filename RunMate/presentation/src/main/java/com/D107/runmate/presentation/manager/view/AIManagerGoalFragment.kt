package com.D107.runmate.presentation.manager.view

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerGoalBinding
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.D107.runmate.presentation.manager.viewmodel.MarathonViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AIManagerGoalFragment : BaseFragment<FragmentAIManagerGoalBinding>(
    FragmentAIManagerGoalBinding::bind,
    R.layout.fragment_a_i_manager_goal
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())
    val viewModel: MarathonViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
    }

    private fun setClickListeners() {
        binding.etTargetMarathon.setOnClickListener{
            findNavController().navigate(R.id.action_aiManagerGoal_to_aiSearchMarathon)
        }

        binding.etTargetDate.setOnClickListener {
            showDatePickerDialog(binding.etTargetDate)
        }

        binding.btnConfirmGoal.setOnClickListener {
            findNavController().navigate(R.id.action_aiManagerGoal_to_aiManager)
        }
    }

    private fun showDatePickerDialog(dateEditText: EditText) {
        val calendar = Calendar.getInstance() // 현재 날짜를 기본으로 설정

        val currentText = dateEditText.text.toString()
        if (currentText.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                calendar.time = sdf.parse(currentText)!!
            } catch (e: Exception) {
                // 파싱 실패 시 현재 날짜 유지
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                val selectedDateTime = selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()

                dateEditText.setText(selectedDateTime.format(formatter))

                viewModel.selectDate(selectedDateTime)
            },
            year,
            month,
            day
        )

        // 최소 날짜 설정 (예: 오늘부터)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 // 어제 자정 이후부터 선택 가능

        // 최대 날짜 설정 (오늘로부터 1년 후까지)
        val maxDateCalendar = Calendar.getInstance()
        maxDateCalendar.add(Calendar.YEAR, 1) // 현재 날짜에 1년 추가
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis

        datePickerDialog.show()
    }
}