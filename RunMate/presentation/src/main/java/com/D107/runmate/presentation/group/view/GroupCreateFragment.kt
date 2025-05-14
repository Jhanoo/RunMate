package com.D107.runmate.presentation.group.view

import android.app.DatePickerDialog
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
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupCreateBinding
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.D107.runmate.presentation.utils.CommonUtils
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class GroupCreateFragment : BaseFragment<FragmentGroupCreateBinding>(
    FragmentGroupCreateBinding::bind,
    R.layout.fragment_group_create) {


    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())
    val viewModel: GroupCreateViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
        setListener()
        observeViewModel()
    }

    private fun updateUI() {
        binding.etGroupName.setText(viewModel.groupName.value)
        binding.etLocation.setText(viewModel.selectedPlace.value?.address?:"")
        binding.etDate.setText(CommonUtils.formatIsoDateToCustom((viewModel.selectedDate.value.toString())))

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
                            val formattedDate = date.format(formatter)
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

            }
        }

    }

    private fun setListener() {
        binding.toolbarGroupCreate.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.etLocation.setOnClickListener{
            findNavController().navigate(R.id.action_groupCreateFragment_to_placeSearchFragment)
        }
        binding.etDate.setOnClickListener{
            showDatePickerDialog(binding.etDate)
        }
        binding.btnCreateGroup.setOnClickListener{
            viewModel.createGroup()
            findNavController().popBackStack()
        }
        binding.etGroupName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onGroupNameChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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

    override fun onDestroy() {
        super.onDestroy()

    }




}