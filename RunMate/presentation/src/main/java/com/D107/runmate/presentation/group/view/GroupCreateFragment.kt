package com.D107.runmate.presentation.group.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupCreateBinding
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class GroupCreateFragment : BaseFragment<FragmentGroupCreateBinding>(
    FragmentGroupCreateBinding::bind,
    R.layout.fragment_group_create) {
    val viewModel: GroupCreateViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedPlace.collect{place->
                binding.etLocation.setText(place?.name)
            }
        }
    }

    private fun setClickListener() {
        binding.toolbarGroupCreate.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.etLocation.setOnClickListener{
            findNavController().navigate(R.id.action_groupCreateFragment_to_placeSearchFragment)
        }
        binding.etDate.setOnClickListener{
            showDatePickerDialog(binding.etDate)
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
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)

                val displayFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val formattedDateString = displayFormat.format(selectedCalendar.time)
                dateEditText.setText(formattedDateString)
                viewModel.selectDate(selectedCalendar.time)


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