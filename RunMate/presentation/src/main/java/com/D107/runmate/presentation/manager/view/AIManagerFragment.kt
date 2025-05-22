package com.D107.runmate.presentation.manager.view

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.D107.runmate.domain.model.manager.ScheduleItem
import com.D107.runmate.domain.repository.manager.TodoRepository
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerBinding
import com.D107.runmate.presentation.manager.adapter.ScheduleRVAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.D107.runmate.data.remote.api.MarathonService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.presentation.manager.util.CurriculumPrefs
import com.D107.runmate.presentation.manager.viewmodel.CurriculumViewModel
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AIManagerFragment : BaseFragment<FragmentAIManagerBinding>(
    FragmentAIManagerBinding::bind,
    R.layout.fragment_a_i_manager
) {
    @Inject
    lateinit var todoRepository: TodoRepository

    @Inject
    lateinit var marathonService: MarathonService

    private lateinit var scheduleAdapter: ScheduleRVAdapter

//    private var canRefresh = false

    // 서버에서 가져온 모든 일정을 저장할 리스트
    private val allScheduleItems = mutableListOf<ScheduleItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 캘린더 초기화
        setupCalendar()

        // 리사이클러뷰 초기화
        initRecyclerView()

        updateRefreshButton()

        updateGoalDist()

        CurriculumPrefs.logAllPreferences(requireContext())

        // 오늘 날짜에 해당하는 데이터 로드
        loadSchedulesForSelectedDate(Calendar.getInstance())
    }

    private fun updateGoalDist() {
        val curriculumViewModel = ViewModelProvider(requireActivity()).get(CurriculumViewModel::class.java)

        // 현재 커리큘럼 정보 가져오기
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                curriculumViewModel.myCurriculum.collect { result ->
                    result?.getOrNull()?.let { curriculum ->
                        // goalDist 값 가져오기
                        val goalDist = curriculum.goalDist

                        // TextView에 설정
                        binding.tvGoalValue.text = goalDist
                    }
                }
            }
        }
    }

    private fun updateRefreshButton() {
        CurriculumPrefs.logAllPreferences(requireContext())

        val canRefresh = CurriculumPrefs.canRefresh(requireContext())

        binding.ivRefresh.apply {
            setImageResource(if (canRefresh) R.drawable.refresh else R.drawable.refresh_no)
            isClickable = canRefresh
            setOnClickListener(if (canRefresh) { _ -> showInformModifyDialog() } else null)
        }
    }

    private fun showInformModifyDialog() {
//        CurriculumPrefs.saveRefreshTime(requireContext())
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_new_curriculum)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        val btnConfirm = dialog.findViewById<Button>(R.id.btn_create_curriculum)

        btnConfirm.setOnClickListener {
            CurriculumPrefs.saveRefreshTime(requireContext())

            dialog.dismiss()

            findNavController().navigate(R.id.AIManagerExpMarathonFragment)
        }
        dialog.show()
    }

//    private fun showCurriculumModifyDialog() {
//        CurriculumPrefs.saveRefreshTime(requireContext())
//        val dialog = Dialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(R.layout.dialog_curriculum_modify)
//
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//
//        val curriculumViewModel = ViewModelProvider(requireActivity()).get(CurriculumViewModel::class.java)
//        val currentCurriculum = curriculumViewModel.myCurriculum.value?.getOrNull()
//
//        if (currentCurriculum == null) {
//            Toast.makeText(requireContext(), "커리큘럼 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
//            dialog.dismiss()
//            return
//        }
//
//        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)
//        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
//
//        val etMarathonName = dialog.findViewById<EditText>(R.id.et_marathon_name)
//        val distanceGroup = dialog.findViewById<RadioGroup>(R.id.distance_options)
//        val prepGroup = dialog.findViewById<RadioGroup>(R.id.preparation_options)
//
//        // 모든 거리 라디오 버튼의 텍스트 색상 초기화
//        for (i in 0 until distanceGroup.childCount) {
//            val radioButton = distanceGroup.getChildAt(i) as RadioButton
//            radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
//        }
//
//        // 모든 준비기간 라디오 버튼의 텍스트 색상 초기화
//        for (i in 0 until prepGroup.childCount) {
//            val radioButton = prepGroup.getChildAt(i) as RadioButton
//            radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
//        }
//
//        // 선택된 버튼의 텍스트 색상 설정
//        distanceGroup.findViewById<RadioButton>(distanceGroup.checkedRadioButtonId)?.setTextColor(
//            ContextCompat.getColor(requireContext(), R.color.white)
//        )
//
//        prepGroup.findViewById<RadioButton>(prepGroup.checkedRadioButtonId)?.setTextColor(
//            ContextCompat.getColor(requireContext(), R.color.white)
//        )
//
//        // 거리 라디오 그룹 선택 리스너 설정
//        distanceGroup.setOnCheckedChangeListener { group, checkedId ->
//            // 모든 버튼 텍스트 색상 초기화
//            for (i in 0 until group.childCount) {
//                val radioButton = group.getChildAt(i) as RadioButton
//                radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
//            }
//
//            // 선택된 버튼 텍스트 색상 변경
//            group.findViewById<RadioButton>(checkedId)?.setTextColor(
//                ContextCompat.getColor(requireContext(), R.color.white)
//            )
//        }
//
//        // 준비기간 라디오 그룹 선택 리스너 설정
//        prepGroup.setOnCheckedChangeListener { group, checkedId ->
//            // 모든 버튼 텍스트 색상 초기화
//            for (i in 0 until group.childCount) {
//                val radioButton = group.getChildAt(i) as RadioButton
//                radioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
//            }
//
//            // 선택된 버튼 텍스트 색상 변경
//            group.findViewById<RadioButton>(checkedId)?.setTextColor(
//                ContextCompat.getColor(requireContext(), R.color.white)
//            )
//        }
//
//        btnClose.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        Timber.d("마라톤 정보 가져오기 시작:  ${currentCurriculum.marathonId}")
//
//        // 마라톤 ID로 마라톤 정보 요청
//        curriculumViewModel.getMarathonById(currentCurriculum.marathonId)
//
//        // 마라톤 정보 가져오기
//        lifecycleScope.launch {
//            // 마라톤 정보 관찰
//            curriculumViewModel.marathonInfo.collect { marathonResult ->
//                marathonResult?.fold(
//                    onSuccess = { marathon ->
//                        Timber.d("마라톤 정보 가져오기 성공: ${marathon.title}")
//
//                        // marathon은 이제 MarathonInfo 타입입니다
//                        etMarathonName.setText(marathon.title)
//
//                        // 현재 거리 선택
//                        when (currentCurriculum.goalDist) {
//                            "5km" -> {
//                                distanceGroup.check(R.id.rb_run_0)
//                                distanceGroup.findViewById<RadioButton>(R.id.rb_run_0)?.setTextColor(
//                                    ContextCompat.getColor(requireContext(), R.color.white)
//                                )
//                            }
//                            "10km" -> {
//                                distanceGroup.check(R.id.rb_run_1_2)
//                                distanceGroup.findViewById<RadioButton>(R.id.rb_run_1_2)?.setTextColor(
//                                    ContextCompat.getColor(requireContext(), R.color.white)
//                                )
//                            }
//                            "21.0975km", "21km" -> {
//                                distanceGroup.check(R.id.rb_run_3_4)
//                                distanceGroup.findViewById<RadioButton>(R.id.rb_run_3_4)?.setTextColor(
//                                    ContextCompat.getColor(requireContext(), R.color.white)
//                                )
//                            }
//                            "42.195km", "42km" -> {
//                                distanceGroup.check(R.id.rb_run_5_more)
//                                distanceGroup.findViewById<RadioButton>(R.id.rb_run_5_more)?.setTextColor(
//                                    ContextCompat.getColor(requireContext(), R.color.white)
//                                )
//                            }
//                        }
//                    },
//                    onFailure = { error ->
//                        Timber.e("마라톤 정보 가져오기 실패: ${error.message}")
//                    }
//                )
//            }
//        }
//
//        btnConfirm.setOnClickListener {
//            CurriculumPrefs.saveRefreshTime(requireContext())
//
//            // 선택된 거리 값 가져오기
//            val goalDist = when (distanceGroup.checkedRadioButtonId) {
//                R.id.rb_run_0 -> "5km"
//                R.id.rb_run_1_2 -> "10km"
//                R.id.rb_run_3_4 -> "21.0975km"
//                R.id.rb_run_5_more -> "42.195km"
//                else -> "10km"
//            }
//
//            // 준비 기간에 따라 목표 날짜 계산
//            val calendar = Calendar.getInstance()
//            val weeksToAdd = when (prepGroup.checkedRadioButtonId) {
//                R.id.rb_prev_0 -> 4
//                R.id.rb_prev_1 -> 6
//                R.id.rb_prev_3 -> 8
//                R.id.rb_prev_4 -> 12
//                R.id.rb_prev_5 -> 20
//                else -> 6
//            }
//            calendar.add(Calendar.WEEK_OF_YEAR, weeksToAdd)
//
//            // ISO 8601 형식으로 변환
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
//            val goalDate = dateFormat.format(calendar.time)
//
//            // 커리큘럼 업데이트
//            val updatedCurriculum = currentCurriculum.copy(
//                goalDist = goalDist,
//                goalDate = goalDate
//            )
//            CurriculumPrefs.saveRefreshTime(requireContext())
//
//            dialog.dismiss()
//
//            findNavController().navigate(
//                R.id.AIManagerLoadingFragment,
//                null,
//                androidx.navigation.NavOptions.Builder()
//                    .setPopUpTo(R.id.AIManagerFragment, true)  // AIManagerFragment에서 시작
//                    .build()
//            )
//
//            curriculumViewModel.updateCurriculum(updatedCurriculum)
//
//            // 결과 관찰
//            lifecycleScope.launch {
//                curriculumViewModel.curriculumCreationResult.collect { result ->
//                    result?.fold(
//                        onSuccess = { curriculumId ->
//                            Timber.d("커리큘럼 업데이트 성공: ${curriculumId}")
//
//                            // AIManagerFragment로 이동
//                            findNavController().navigate(
//                                R.id.AIManagerFragment,
//                                bundleOf("curriculumId" to curriculumId),
//                                androidx.navigation.NavOptions.Builder()
//                                    .setPopUpTo(R.id.AIManagerLoadingFragment, true)
//                                    .build()
//                            )
//
//                            updateRefreshButton()
//                        },
//                        onFailure = { error ->
//                            Timber.e("커리큘럼 업데이트 실패: ${error.message}")
//                            Toast.makeText(requireContext(), "커리큘럼 업데이트 실패: ${error.message}", Toast.LENGTH_SHORT).show()
//
//                            // 오류 발생 시 AIManagerFragment로 돌아가기
//                            findNavController().popBackStack()
//                        }
//                    )
//                }
//            }
//
//
//            Toast.makeText(context, "목표가 수정되었습니다", Toast.LENGTH_SHORT).show()
//        }
//
//        dialog.show()
//    }

    override fun onResume() {
        super.onResume()
        updateRefreshButton()
    }

    private fun setupCalendar() {
        // 캘린더 타이틀 형식 설정
        binding.calendar.setTitleFormatter { day ->
            val year = day.year
            val month = day.month + 1
            "%04d.%02d".format(year, month)
        }

        // 현재 날짜 가져오기
        val today = CalendarDay.today()

        // Make selection color transparent to disable default rectangle selection
        binding.calendar.setSelectionColor(Color.TRANSPARENT)

        // 현재 날짜 이후의 날짜는 선택 불가능하게 설정
        binding.calendar.setDateSelected(today, true)

        // 달력에서 날짜 선택 이벤트 처리
        binding.calendar.setOnDateChangedListener { _, date, _ ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(date.year, date.month, date.day)
            }
            binding.calendar.removeDecorators()
            loadSchedulesForSelectedDate(selectedCalendar)
        }
    }

    private fun initRecyclerView() {
        scheduleAdapter = ScheduleRVAdapter()
        binding.recyclerScheduleList.adapter = scheduleAdapter

        // 아이템 클릭 리스너 설정
        scheduleAdapter.setItemClickListener(object : ScheduleRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: ScheduleItem, position: Int) {
                // 체크박스 상태 변경 시 수행할 작업
                val message = if (data.isCompleted ?: false) "일정 완료!" else "일정 미완료로 변경"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                // TODO: 완료 상태 변경 API 호출
                // updateTodoStatus(data.todoId, data.isCompleted)
            }
        })
    }

    // 선택한 날짜에 해당하는 일정 로드
    private fun loadSchedulesForSelectedDate(selectedDate: Calendar) {
        // allScheduleItems 초기화 - 중요!
        allScheduleItems.clear()
        scheduleAdapter.submitList(emptyList())

//        lifecycleScope.launch(Dispatchers.Main) {
//            // UI 즉시 비우기
//            scheduleAdapter.submitList(emptyList())
//        }

        // 선택한 날짜가 속한 주의 월요일과 일요일 구하기
        val monday = getWeekMonday(selectedDate)
        val sunday = getWeekSunday(selectedDate)

        // 주 단위로 필요한 모든 월을 확인
        val mondayMonth = monday.get(Calendar.MONTH) + 1  // 0-based 인덱스라 +1
        val sundayMonth = sunday.get(Calendar.MONTH) + 1  // 0-based 인덱스라 +1
        val mondayYear = monday.get(Calendar.YEAR)
        val sundayYear = sunday.get(Calendar.YEAR)

        // 해당 주에 포함된 모든 월의 일정을 로드
        lifecycleScope.launch {
            // 월요일이 속한 달의 일정 로드
            loadMonthSchedules(mondayYear, mondayMonth, selectedDate)

            // 일요일이 다른 달에 있는 경우 해당 달의 일정도 로드
            if (mondayMonth != sundayMonth || mondayYear != sundayYear) {
                loadMonthSchedules(sundayYear, sundayMonth, selectedDate)
            }
        }
    }

    private fun loadMonthSchedules(year: Int, month: Int, selectedDate: Calendar) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    timber.log.Timber.d("TodoRepository API 호출: getTodoList($year, $month)")

                    todoRepository.getTodoList(year, month).collect { result ->
                        result.fold(
                            onSuccess = { todoList ->
                                timber.log.Timber.d("API 호출 성공: ${todoList.size}개 일정 수신")

                                // API 응답을 ScheduleItem 리스트로 변환하여 추가
                                val scheduleItems = convertToScheduleItems(todoList)

                                withContext(Dispatchers.Main) {
                                    // 기존 아이템 목록에 추가
                                    allScheduleItems.addAll(scheduleItems)

                                    // 주 단위로 필터링하여 표시
                                    filterAndDisplaySchedules(selectedDate)
                                }
                            },
                            onFailure = { error ->
                                timber.log.Timber.e("API 호출 실패: ${error.message}")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "일정을 불러오는데 실패했습니다: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                timber.log.Timber.e("예외 발생: ${e.message}")
                timber.log.Timber.e(e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // API 응답을 ScheduleItem 리스트로 변환
    private fun convertToScheduleItems(todoList: List<com.D107.runmate.domain.model.manager.TodoItem>): List<ScheduleItem> {
        timber.log.Timber.d("convertToScheduleItems 시작: ${todoList.size}개 항목")

        return todoList.mapNotNull { todo ->
            try {
                timber.log.Timber.d("일정 변환 시도: ${todo.todoId}, date=${todo.date}")

                val date = java.time.OffsetDateTime.parse(todo.date)
                val formattedDate = "${date.monthValue}/${date.dayOfMonth}"
                val formattedDay = when(date.dayOfWeek) {
                    java.time.DayOfWeek.MONDAY -> "월"
                    java.time.DayOfWeek.TUESDAY -> "화"
                    java.time.DayOfWeek.WEDNESDAY -> "수"
                    java.time.DayOfWeek.THURSDAY -> "목"
                    java.time.DayOfWeek.FRIDAY -> "금"
                    java.time.DayOfWeek.SATURDAY -> "토"
                    java.time.DayOfWeek.SUNDAY -> "일"
                }

                timber.log.Timber.d("날짜 변환 성공: ${todo.date} -> $formattedDate ($formattedDay)")

                ScheduleItem(
                    date = formattedDate,
                    day = formattedDay,
                    scheduleText = todo.content,
//                    isCompleted = true,
                    isCompleted = todo.isDone
//                    todoId = todo.todoId
                )
            } catch (e: Exception) {
                android.util.Log.e("AIManagerFragment", "날짜 변환 오류: ${e.message}, 날짜: ${todo.date}")
                null
            }
        }
    }

    // 선택한 날짜에 해당하는 일정만 필터링하여 표시
    private fun filterAndDisplaySchedules(selectedDate: Calendar) {
        try {
            // 선택한 날짜가 속한 주의 월요일 구하기
            val monday = getWeekMonday(selectedDate)

            // 선택한 날짜가 속한 주의 일요일 구하기
            val sunday = getWeekSunday(selectedDate)

            // 월요일부터 일요일까지의 날짜를 리스트로 생성
            val datesInWeek = ArrayList<Calendar>()
            val tempDate = monday.clone() as Calendar

            // 월요일부터 시작해서 일요일까지만 추가
            while (!tempDate.after(sunday)) {
                datesInWeek.add(tempDate.clone() as Calendar)
                tempDate.add(Calendar.DAY_OF_MONTH, 1)
            }

            // 각 날짜를 "M/d" 형식으로 변환
            val dayFormat = SimpleDateFormat("M/d", Locale.getDefault())
            val dateStringsInWeek = datesInWeek.map { dayFormat.format(it.time) }

            val selectedDateStr = dayFormat.format(selectedDate.time)
            timber.log.Timber.d("선택된 날짜: $selectedDateStr")

//            timber.log.Timber.d("주간 날짜 목록: $dateStringsInWeek")
//            timber.log.Timber.d("전체 일정 수: ${allScheduleItems.size}")

            // 선택한 주의 일정만 필터링
            val filteredSchedules = allScheduleItems.filter { item ->
                val match = dateStringsInWeek.contains(item.date)
                timber.log.Timber.d("일정 비교: 일정 날짜=${item.date}, 포함여부=${match}")
                match
            }

            // 날짜별로 정렬
            val sortedSchedules = filteredSchedules.sortedBy { item ->
                val parts = item.date.split("/")
                val month = parts[0].toInt()
                val day = parts[1].toInt()
                month * 100 + day // 월과 일을 조합하여 정렬
            }

            timber.log.Timber.d("필터링 결과: ${sortedSchedules.size}개 일정 표시")

            // 리사이클러뷰에 표시
            scheduleAdapter.submitList(sortedSchedules) {
                // 선택된 날짜의 position 찾기
                val selectedDateStr = SimpleDateFormat("M/d", Locale.getDefault()).format(selectedDate.time)
                val selectedPosition = sortedSchedules.indexOfFirst { it.date == selectedDateStr }

                timber.log.Timber.d("선택된 날짜 위치: $selectedPosition (선택된 날짜: $selectedDateStr)")

                // 선택된 위치 설정
                scheduleAdapter.setSelectedPosition(selectedPosition)

                // 해당 위치로 스크롤 (선택된 아이템이 있는 경우)
                if (selectedPosition != -1) {
                    binding.recyclerScheduleList.post {
                        // LinearLayoutManager를 사용해 선택된 아이템이 맨 위에 오도록 스크롤
                        (binding.recyclerScheduleList.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.scrollToPositionWithOffset(selectedPosition, 0)
                    }
                }
            }

            updateCalendarWithCompletedTasks()

        } catch (e: Exception) {
            timber.log.Timber.e("일정 필터링 중 오류: ${e.message}")
            timber.log.Timber.e(e)

            // 오류 발생 시 빈 리스트 표시
            scheduleAdapter.submitList(emptyList())
        }
    }

    private fun getWeekSunday(selectedDate: Calendar): Calendar {
        val result = Calendar.getInstance()
        result.timeInMillis = selectedDate.timeInMillis
        val dayOfWeek = result.get(Calendar.DAY_OF_WEEK)

        // Calendar.MONDAY는 2, Calendar.SUNDAY는 1
        val daysToAdd = when (dayOfWeek) {
            Calendar.SUNDAY -> 0 // 이미 일요일
            else -> Calendar.SATURDAY + 1 - dayOfWeek // 토요일과의 차이 + 1
        }

        result.add(Calendar.DAY_OF_MONTH, daysToAdd)
        return result
    }

    private fun getWeekMonday(selectedDate: Calendar): Calendar {
        val result = Calendar.getInstance()
        result.timeInMillis = selectedDate.timeInMillis
        val dayOfWeek = result.get(Calendar.DAY_OF_WEEK)

        // Calendar.MONDAY는 2, Calendar.SUNDAY는 1
        val daysToSubtract = when (dayOfWeek) {
            Calendar.SUNDAY -> 6 // 일요일이면 6일 전이 월요일
            else -> dayOfWeek - Calendar.MONDAY // 다른 날은 해당 요일 - 월요일
        }

        result.add(Calendar.DAY_OF_MONTH, -daysToSubtract)
        return result
    }

    private class CompletedDayDecorator(
        private val dates: Collection<CalendarDay>,
        private val selectedDate: CalendarDay?
    ) : DayViewDecorator {
        private val lightGreenColor = Color.parseColor(("#FFEBF8F2"))

        override fun shouldDecorate(day: CalendarDay): Boolean {
            // Only decorate completed days that are NOT the selected date
            return dates.contains(day) && day != selectedDate
        }

        override fun decorate(view: DayViewFacade) {
            val roundedBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
//                setSize(32,32)
                cornerRadius = 4f  // Rounded corners
                setColor(lightGreenColor)

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    setPadding(2,2,2,2)
//                }
            }
            val insetDrawable = android.graphics.drawable.InsetDrawable(
                roundedBackground,
                1,  // 왼쪽 여백
                1,  // 위쪽 여백
                1,  // 오른쪽 여백
                1   // 아래쪽 여백
            )
            view.setBackgroundDrawable(insetDrawable)
        }
    }

    private fun updateCalendarWithCompletedTasks() {
        val completedDays = mutableSetOf<CalendarDay>()
        val selectedDate = binding.calendar.selectedDate

        allScheduleItems.forEach { item ->
            if (item.isCompleted == true) {
                try {
                    val parts = item.date.split("/")
                    val month = parts[0].toInt() - 1 // Calendar month is 0-based
                    val day = parts[1].toInt()

                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)

                    completedDays.add(CalendarDay.from(year, month, day))
                } catch (e: Exception) {
                    timber.log.Timber.e("Error parsing date: ${item.date}")
                }
            }
        }

//        binding.calendar.removeDecorators()

        if (completedDays.isNotEmpty()) {
            binding.calendar.addDecorator(CompletedDayDecorator(completedDays, binding.calendar.selectedDate))
        }

        if (selectedDate != null) {
            binding.calendar.addDecorator(object : DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay): Boolean {
                    return day == selectedDate
                }

                override fun decorate(view: DayViewFacade) {
                    val selectionColor = ContextCompat.getColor(requireContext(), R.color.login_btn)

                    val selectionBackground = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 30f
                        setColor(selectionColor)
                    }
                    view.setSelectionDrawable(selectionBackground)
                }
            })
        }

        binding.calendar.setDateSelected(selectedDate, true)
    }
}