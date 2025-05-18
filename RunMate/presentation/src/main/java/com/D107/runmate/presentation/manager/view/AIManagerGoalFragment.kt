package com.D107.runmate.presentation.manager.view

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.transition.Visibility
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerGoalBinding
import com.D107.runmate.presentation.manager.util.CurriculumPrefs
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
import java.time.Year
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

    private var selectedMarathonId = ""
    private var selectedDate: OffsetDateTime? = null
    private var selectedDistance = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupMarathonResultListener()
        setClickListeners()

        if (selectedMarathonId.isEmpty()) {
            val defaultDistances = ArrayList<String>().apply {
                add("5km")
                add("10km")
                add("하프(21km)")
                add("풀(42km)")
            }
            updateDistanceOptions(defaultDistances)
        }

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
            val marathonDate = bundle.getString("marathonDate") ?: return@setFragmentResultListener
            val marathonDistances = bundle.getStringArrayList("marathonDistances") ?: ArrayList()

            // 선택된 마라톤 ID 저장
            selectedMarathonId = marathonId

            binding.btnCloseMarathon.visibility = View.VISIBLE
            binding.etTargetDate.isEnabled = false
            binding.etTargetDate.isFocusable = false
            binding.etTargetDate.isClickable = false

            val truncatedTitle = if (marathonTitle.length > 20) {
                "${marathonTitle.substring(0, 20)}···"
            } else {
                marathonTitle
            }

            binding.etTargetMarathon.setText(truncatedTitle)

            // 마라톤 날짜 설정
            try {
                // 날짜 형식 변환 ("2025-05-17" -> "2025.05.17")
                val dateFormatters = listOf(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("M/d", Locale.getDefault()),
                    DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
                )

                var parsedDate: LocalDate? = null
                var formatFound = false

                for (formatter in dateFormatters) {
                    try {
                        parsedDate = LocalDate.parse(marathonDate, formatter)
                        formatFound = true
                        break
                    } catch (e: Exception) {
                        // Try next format
                    }
                }

                if (!formatFound) {
                    val parts = marathonDate.split("/")
                    if (parts.size == 2) {
                        try {
                            val month = parts[0].toInt()
                            val day = parts[1].toInt()
                            // 현재 연도 사용
                            val year = Year.now().value
                            parsedDate = LocalDate.of(year, month, day)
                            formatFound = true
                        } catch (e: Exception) {
                            Timber.e("수동 날짜 파싱 실패: ${e.message}")
                        }
                    }
                }

                if (parsedDate != null) {
                    val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                    val formattedDate = parsedDate.format(outputFormatter)

                    binding.etTargetDate.setText(formattedDate)

                    // 선택된 날짜 저장
                    selectedDate =
                        parsedDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()
                } else {
                    Timber.e("날짜 파싱 실패: $marathonDate")
                    Toast.makeText(
                        requireContext(),
                        "날짜 형식을 인식할 수 없습니다: $marathonDate",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Timber.e("날짜 변환 오류: ${e.message}")
                Toast.makeText(requireContext(), "날짜 변환 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }

            // 거리 선택 업데이트
            updateDistanceOptions(marathonDistances)
        }
    }

    // 거리 옵션 동적 생성 함수
    private fun updateDistanceOptions(distances: ArrayList<String>) {
        // 기존 라디오 그룹 초기화
        binding.rgMaxDistance.removeAllViews()

        val flexboxLayout = binding.rgMaxDistance as ViewGroup

        // 라디오 버튼 그룹 관리를 위한 변수
        val radioButtons = mutableListOf<RadioButton>()

        // 동적으로 RadioButton 생성
        for ((index, distance) in distances.withIndex().take(6)) {
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()

                // 고정 너비 레이아웃 설정
                val layoutParams = ViewGroup.MarginLayoutParams(
                    (83 * resources.displayMetrics.density).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                if (index > 0) {
                    layoutParams.marginStart = 8
                }
                layoutParams.topMargin = 16
//                layoutParams.bottomMargin = 10
                layoutParams.marginEnd = 10
                this.layoutParams = layoutParams

//                text = distance
                text = when {
                    distance.contains("하프") && distance.contains("21.0975") -> "하프(21km)"
                    distance.contains("하프") && !distance.contains("(") -> "하프(21km)"
                    distance.contains("풀") && !distance.contains("(") -> "풀(42km)"
                    else -> distance
                }
                setBackgroundResource(R.drawable.radio_selector)
                buttonDrawable = null
                gravity = android.view.Gravity.CENTER
//                setPadding(0, 0, 10, 0)

                val isLongText = distance.length > 7
                textSize = if (isLongText) 12f else 14f

                // 수동 라디오 버튼 동작 처리
                setOnClickListener {
                    // 다른 모든 버튼 선택 해제
                    radioButtons.forEach { btn ->
                        btn.isChecked = (btn == this)
                        btn.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                if (btn.isChecked) android.R.color.white else android.R.color.black
                            )
                        )
                    }
                    selectedDistance = distance
                }

                // 첫 번째 항목은 처음부터 선택 상태로 설정
                isChecked = index == 0
                if (index == 0) {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    selectedDistance = distance
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }

            radioButtons.add(radioButton)
            flexboxLayout.addView(radioButton)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    curriculumViewModel.curriculumCreationResult.collect { result ->
                        result?.fold(
                            onSuccess = { curriculumId ->
                                CurriculumPrefs.saveRefreshTime(requireContext())

                                // 성공 시 로딩 화면에서 AIManagerFragment로 이동
                                Timber.d("커리큘럼 생성 성공: 생성된 ID = $curriculumId")
                                findNavController().navigate(
                                    R.id.AIManagerFragment,
                                    bundleOf("curriculumId" to curriculumId),
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.AIManagerIntroFragment, true)
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
        binding.toolbarGoalTitle.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.etTargetMarathon.setOnClickListener {
            binding.etTargetMarathon.setText("")
            binding.etTargetDate.setText("")
            selectedMarathonId = ""
            selectedDistance = ""
            selectedDate = null

            binding.btnCloseMarathon.visibility = View.GONE
            binding.etTargetDate.isEnabled = true
            binding.etTargetDate.isFocusable = true
            binding.etTargetDate.isClickable = true

            binding.rgMaxDistance.removeAllViews()

            findNavController().navigate(R.id.action_aiManagerGoal_to_aiSearchMarathon)
        }

        binding.btnCloseMarathon.setOnClickListener {
            // 선택된 마라톤 정보 초기화
            selectedMarathonId = ""
            binding.etTargetMarathon.setText("")

            // 거리 옵션도 초기화
            binding.rgMaxDistance.removeAllViews()
            selectedDistance = ""

            val defaultDistances = ArrayList<String>().apply {
                add("5km")
                add("10km")
                add("하프(21km)")
                add("풀(42km)")
            }
            updateDistanceOptions(defaultDistances)

            // 날짜 필드 활성화 및 초기화
            binding.btnCloseMarathon.visibility = View.GONE
            binding.etTargetDate.isEnabled = true
            binding.etTargetDate.isFocusable = true
            binding.etTargetDate.isClickable = true
        }

        binding.etTargetDate.setOnClickListener {
            showDatePickerDialog(binding.etTargetDate)
        }

        binding.btnConfirmGoal.setOnClickListener {
            createCurriculum()
        }
    }

    private fun createCurriculum() {
        if (selectedDate == null && binding.etTargetDate.text.isNotEmpty()) {
            // Try to parse from the EditText if the selectedDate is somehow null
            try {
                val dateText = binding.etTargetDate.text.toString()
                val localDate = LocalDate.parse(dateText, formatter)
                selectedDate = localDate.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()
            } catch (e: Exception) {
                Timber.e("날짜 변환 오류 (from EditText): ${e.message}")
            }
        }

        val goalDate = selectedDate?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            ?: java.time.OffsetDateTime.now().plusMonths(1)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        // 선택된 거리 가져오기
        val goalDist = selectedDistance ?: "10km"

        // 선택된 마라톤 ID가 없는 경우 사용자에게 알림
//        if (selectedMarathonId.isEmpty()) {
//            Toast.makeText(requireContext(), "마라톤을 선택해주세요", Toast.LENGTH_SHORT).show()
//            return
//        }

        Timber.d("커리큘럼 생성 요청: marathonId=$selectedMarathonId, goalDist=$goalDist, goalDate=$goalDate")

        findNavController().navigate(R.id.AIManagerLoadingFragment)

        findNavController().navigate(
            R.id.AIManagerLoadingFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.AIManagerIntroFragment, true)  // IntroFragment부터 모두 제거
                .build()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            delay(10000) // 30초 후
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
                    Toast.makeText(requireContext(), "커리큘럼 생성 결과를 확인할 수 없습니다.", Toast.LENGTH_SHORT)
                        .show()
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
        maxDateCalendar.add(Calendar.MONTH, 3) // 현재 날짜에 3개월 추가
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis

        datePickerDialog.show()
    }
}