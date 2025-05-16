package com.D107.runmate.presentation.manager.view

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerGoalBinding
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.D107.runmate.presentation.manager.viewmodel.CurriculumViewModel
import com.D107.runmate.presentation.manager.viewmodel.MarathonViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
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
    val curriculumViewModel: CurriculumViewModel by activityViewModels()

    private var selectedMarathonId = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    private var selectedDate: OffsetDateTime? = null
    private var selectedDistance = "10km"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupMarathonResultListener()
        setClickListeners()

        lifecycleScope.launch {
            delay(5000) // 애니메이션을 위한 지연
            curriculumViewModel.getMyCurriculum()
        }
    }

    private fun setupMarathonResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "marathonSearchResult",
            viewLifecycleOwner
        ) { _, bundle ->
            // 검색 결과에서 데이터 추출
            val marathonId = bundle.getString("marathonId") ?: return@setFragmentResultListener
            val marathonTitle =
                bundle.getString("marathonTitle") ?: return@setFragmentResultListener

            // 선택된 마라톤 ID 저장
            selectedMarathonId = marathonId

            // UI 업데이트
            binding.etTargetMarathon.setText(marathonTitle)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    curriculumViewModel.curriculumCreationResult.collect { result ->
                        result?.fold(
                            onSuccess = { curriculumId ->
                                // 성공 시 로딩 화면에서 AIManagerFragment로 이동
                                Timber.d("커리큘럼 생성 성공: 생성된 ID = $curriculumId")
                                findNavController().navigate(
                                    R.id.AIManagerFragment,
                                    bundleOf("curriculumId" to curriculumId),
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.AIManagerLoadingFragment, true)
                                        .build()
                                )
                            },
                            onFailure = { error ->
                                // 실패 시 에러 메시지 표시 후 로딩 화면에서 뒤로가기
                                Timber.e("커리큘럼 생성 실패: ${error.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "커리큘럼 생성 실패: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 로딩 화면에서 빠져나오기
                                findNavController().popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.etTargetMarathon.setOnClickListener {
            findNavController().navigate(R.id.action_aiManagerGoal_to_aiSearchMarathon)
        }

        binding.etTargetDate.setOnClickListener {
            showDatePickerDialog(binding.etTargetDate)
        }

        binding.rgMaxDistance.setOnCheckedChangeListener { _, checkedId ->
            selectedDistance = when (checkedId) {
                R.id.rb_distance_5km -> "5km"
                R.id.rb_distance_10km -> "10km"
                R.id.rb_distance_half -> "하프"
                R.id.rb_distance_full -> "풀"
                else -> "10km"
            }
        }

        binding.btnConfirmGoal.setOnClickListener {
            createCurriculum()
//            findNavController().navigate(R.id.action_aiManagerGoal_to_aiManager)
        }
    }

    private fun createCurriculum() {
        val goalDate = selectedDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            ?: "2025-06-10T09:00:00+09:00" // 기본값

        // 선택된 거리 가져오기
        val goalDist = when (binding.rgMaxDistance.checkedRadioButtonId) {
            R.id.rb_distance_5km -> "5km"
            R.id.rb_distance_10km -> "10km"
            R.id.rb_distance_half -> "하프"
            R.id.rb_distance_full -> "풀"
            else -> "10km"
        }

        Timber.d("커리큘럼 생성 요청: marathonId=$selectedMarathonId, goalDist=$goalDist, goalDate=$goalDate")

        findNavController().navigate(R.id.AIManagerLoadingFragment)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(30000) // 30초 후
            // 성공 응답이 왔으나 화면이 전환되지 않은 경우를 위한 안전장치
            if (findNavController().currentDestination?.id == R.id.AIManagerLoadingFragment) {
                // 현재 로딩 화면에 있다면 강제로 다음 화면으로 이동
                curriculumViewModel.getMyCurriculum() // 최신 커리큘럼 정보 가져오기
                delay(500) // 잠시 대기
                // 최신 커리큘럼 ID 확인
                curriculumViewModel.myCurriculum.value?.getOrNull()?.let { curriculum ->
                    Timber.d("timeout으로 인한 강제 이동: ${curriculum.curriculumId}")
                    curriculumViewModel.forceNavigateToCurriculumView(curriculum.curriculumId)
                } ?: run {
                    // 커리큘럼 ID를 가져올 수 없는 경우 이전 화면으로 돌아가기
                    findNavController().popBackStack()
                    Toast.makeText(requireContext(), "커리큘럼 생성 결과를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        curriculumViewModel.createCurriculum(
            marathonId = selectedMarathonId,
            goalDist = goalDist,
            goalDate = goalDate
        )
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
                val selectedLocalDate =
                    LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                val selectedDateTime =
                    selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()

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