package com.D107.runmate.presentation.group.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupCreateBinding
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.SourceScreen
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class GroupCreateFragment : BaseFragment<FragmentGroupCreateBinding>(
    FragmentGroupCreateBinding::bind,
    R.layout.fragment_group_create) {


    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.getDefault())
    val viewModel: GroupCreateViewModel by activityViewModels()
    val mainViewModel : MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
        setListener()
        observeViewModel()
    }

    private fun updateUI() {
        binding.etGroupName.setText(viewModel.groupName.value)
        binding.etLocation.setText(viewModel.selectedPlace.value?.address?:"")
        if(viewModel.selectedDate.value!=null) {
            binding.etDate.setText(dateTimeFormatter.format(viewModel.selectedDate.value))
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.groupName.collect { name ->
                        if (binding.etGroupName.text.toString() != name) {
                            binding.etGroupName.setText(name)
                            binding.etGroupName.setSelection(name.length) // 커서를 텍스트 끝으로 이동
                        }
                    }
                }

                launch {
                    viewModel.selectedPlace.collect { place ->
                        binding.etLocation.setText(place?.address)
                    }
                }

                launch {
                    viewModel.selectedDate.collect { date ->
                        if (date != null) {
                            val formattedDate = date.format(dateTimeFormatter)
                            if (binding.etDate.text.toString() != formattedDate) {
                                binding.etDate.setText(formattedDate)
                            }
                        } else {
                            binding.etDate.setText("") // 날짜가 선택되지 않았으면 비움
                        }
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is GroupUiEvent.CreationSuccess -> {
                                findNavController().popBackStack()
                            }

                            is GroupUiEvent.ShowToast -> {
                                showToast(event.message)
                            }
                            else -> {}

                        }
                    }
                }
                launch{
                    viewModel.selectedCourse.collect{courseDetail->
                        binding.etCourse.setText(courseDetail?.name?:"")
                    }
                }

            }
        }

    }

    private fun setListener() {
        binding.toolbarGroupCreate.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.etCourse.setOnClickListener{
            mainViewModel.setSourceScreen(SourceScreen.GROUP_CREATE_FRAGMENT)
            findNavController().navigate(R.id.action_groupCreateFragment_to_courseSettingFragment)
        }
        binding.etLocation.setOnClickListener{
            findNavController().navigate(R.id.action_groupCreateFragment_to_placeSearchFragment)
        }
        binding.etDate.setOnClickListener{
            showDatePickerDialog()
        }
        binding.btnCreateGroup.setOnClickListener{
            viewModel.createGroup()
        }
        binding.etGroupName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onGroupNameChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun showDatePickerDialog() {
        val currentDateTime = viewModel.selectedDate.value
        val calendar = Calendar.getInstance()

        currentDateTime?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                showTimePickerDialog(selectedYear, selectedMonth, selectedDayOfMonth)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        val maxDateCalendar = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun showTimePickerDialog(year: Int, month: Int, dayOfMonth: Int) {
        val currentDateTime = viewModel.selectedDate.value
        val calendar = Calendar.getInstance()

        currentDateTime?.let {
            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
            calendar.set(Calendar.MINUTE, it.minute)
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // 또는 CLOCK_24H
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("시간 선택")
            .setTheme(R.style.BaseTheme_TimePicker)
            .build()


        picker.addOnPositiveButtonClickListener {
            val selectedHour = picker.hour
            val selectedMinute = picker.minute
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            val selectedTime = LocalTime.of(selectedHour, selectedMinute)
            val selectedLocalDateTime = LocalDateTime.of(selectedDate, selectedTime)
            val selectedOffsetDateTime =
                selectedLocalDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime()

            viewModel.selectDate(selectedOffsetDateTime)
        }

        picker.show(parentFragmentManager, "MaterialTimePicker")
//        val currentDateTime = viewModel.selectedDate.value
//        val calendar = Calendar.getInstance()
//
//        currentDateTime?.let {
//            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
//            calendar.set(Calendar.MINUTE, it.minute)
//        }
//
//        val hour = calendar.get(Calendar.HOUR_OF_DAY)
//        val minute = calendar.get(Calendar.MINUTE)
//
//        val timePickerDialog = TimePickerDialog(
//            requireContext(),
//            { _, selectedHour, selectedMinute ->
//                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
//                val selectedTime = LocalTime.of(selectedHour, selectedMinute)
//                val selectedLocalDateTime = LocalDateTime.of(selectedDate, selectedTime)
//                val selectedOffsetDateTime = selectedLocalDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime()
//
//                viewModel.selectDate(selectedOffsetDateTime)
//            },
//            hour,
//            minute,
//            true
//        )
//        timePickerDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()

    }




}