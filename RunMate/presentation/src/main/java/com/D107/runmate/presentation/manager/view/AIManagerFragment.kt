package com.D107.runmate.presentation.manager.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
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

@AndroidEntryPoint
class AIManagerFragment : BaseFragment<FragmentAIManagerBinding>(
    FragmentAIManagerBinding::bind,
    R.layout.fragment_a_i_manager
) {
    @Inject
    lateinit var todoRepository: TodoRepository

    private lateinit var scheduleAdapter: ScheduleRVAdapter
    private val calendar = Calendar.getInstance()
    private val dayOfWeekMap = mapOf(
        Calendar.MONDAY to "월",
        Calendar.TUESDAY to "화",
        Calendar.WEDNESDAY to "수",
        Calendar.THURSDAY to "목",
        Calendar.FRIDAY to "금",
        Calendar.SATURDAY to "토",
        Calendar.SUNDAY to "일"
    )

    // 서버에서 가져온 모든 일정을 저장할 리스트
    private val allScheduleItems = mutableListOf<ScheduleItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 캘린더 초기화
        setupCalendar()

        // 리사이클러뷰 초기화
        initRecyclerView()

        // 오늘 날짜에 해당하는 데이터 로드
        loadSchedulesForSelectedDate(Calendar.getInstance())
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

        // 현재 날짜 이후의 날짜는 선택 불가능하게 설정
        binding.calendar.setDateSelected(today, true)

        // 달력에서 날짜 선택 이벤트 처리
        binding.calendar.setOnDateChangedListener { _, date, _ ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(date.year, date.month, date.day)
            }
            loadSchedulesForSelectedDate(selectedCalendar)

            // 오늘 날짜를 포함한 주의 월요일부터의 날짜를 계산
//            val thisWeekMonday = getThisWeekMonday()

            // 선택한 날짜가 이번 주 월요일보다 이전이거나 같고, 오늘보다 이전이거나 같은 경우만 처리
//            if (!selectedCalendar.after(Calendar.getInstance()) && !selectedCalendar.before(
//                    thisWeekMonday
//                )
//            ) {
//                loadSchedulesForSelectedDate(selectedCalendar)
//            } else if (selectedCalendar.after(Calendar.getInstance())) {
//                Toast.makeText(context, "미래 날짜는 선택할 수 없습니다", Toast.LENGTH_SHORT).show()
//                binding.calendar.setDateSelected(today, true)
//            }
        }
    }

    private fun initRecyclerView() {
        scheduleAdapter = ScheduleRVAdapter()
        binding.recyclerScheduleList.adapter = scheduleAdapter

        // 아이템 클릭 리스너 설정
        scheduleAdapter.setItemClickListener(object : ScheduleRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: ScheduleItem, position: Int) {
                // 체크박스 상태 변경 시 수행할 작업
                val message = if (data.isCompleted) "일정 완료!" else "일정 미완료로 변경"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                // TODO: 완료 상태 변경 API 호출
                // updateTodoStatus(data.todoId, data.isCompleted)
            }
        })
    }

    // 선택한 날짜에 해당하는 일정 로드
    private fun loadSchedulesForSelectedDate(selectedDate: Calendar) {
        lifecycleScope.launch(Dispatchers.Main) {
            scheduleAdapter.submitList(emptyList())
        }

        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH) + 1

        // API 호출 (실제 구현에서는 ViewModel을 통해 호출)
        loadScheduleFromApi(year, month, selectedDate)
    }

    // API에서 일정 데이터 로드
    private fun loadScheduleFromApi(year: Int, month: Int, selectedDate: Calendar) {
        timber.log.Timber.d("일정 로드 시작: year=$year, month=$month")

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    timber.log.Timber.d("TodoRepository API 호출: getTodoList($year, $month)")

                    // TodoRepository를 통한 API 호출
                    todoRepository.getTodoList(year, month).collect { result ->
                        result.fold(
                            onSuccess = { todoList ->
                                timber.log.Timber.d("API 호출 성공: ${todoList.size}개 일정 수신")

                                // API 응답을 ScheduleItem 리스트로 변환
                                allScheduleItems.clear()
                                allScheduleItems.addAll(convertToScheduleItems(todoList))

                                // 선택한 날짜에 해당하는 일정만 필터링하여 표시
                                withContext(Dispatchers.Main) {
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

                // ISO 날짜 문자열에서 날짜 추출 (예: 2025-05-16T00:00:00+09:00)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val date = dateFormat.parse(todo.date) ?: return@mapNotNull null

                if (date == null) {
                    timber.log.Timber.w("날짜 파싱 실패: ${todo.date}")
                    return@mapNotNull null
                }

                // 표시용 날짜 및 요일 포맷
                val dayFormat = SimpleDateFormat("M/d", Locale.getDefault())
                val weekDayFormat = SimpleDateFormat("E", Locale.KOREAN)

                val formattedDate = dayFormat.format(date)
                val formattedDay = weekDayFormat.format(date)

                timber.log.Timber.d("날짜 변환 성공: ${todo.date} -> $formattedDate ($formattedDay)")

                ScheduleItem(
                    date = formattedDate,
                    day = formattedDay,
                    scheduleText = todo.content,
                    isCompleted = todo.isDone,
                    todoId = todo.todoId
                )
            } catch (e: Exception) {
                android.util.Log.e("AIManagerFragment", "날짜 변환 오류: ${e.message}, 날짜: ${todo.date}")
                null
            }
        }
    }


    // 선택한 날짜에 해당하는 일정만 필터링하여 표시
    private fun filterAndDisplaySchedules(selectedDate: Calendar) {
        val selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
        timber.log.Timber.d("일정 필터링: 선택된 날짜=$selectedDateStr, 전체 일정 수=${allScheduleItems.size}")

        try {
            // 선택한 날짜의 월/일 형식 문자열
            val dayFormat = SimpleDateFormat("M/d", Locale.getDefault())
            val selectedDateFormatted = dayFormat.format(selectedDate.time)

            timber.log.Timber.d("선택된 날짜 포맷: $selectedDateFormatted")

            // 선택한 날짜와 일치하는 일정만 필터링
            val filteredSchedules = allScheduleItems.filter { item ->
                val match = item.date == selectedDateFormatted
                timber.log.Timber.d("일정 비교: 일정 날짜=${item.date}, 선택 날짜=$selectedDateFormatted, 일치=${match}")
                match
            }

            timber.log.Timber.d("필터링 결과: ${filteredSchedules.size}개 일정 표시")

            // 리사이클러뷰에 표시
            scheduleAdapter.submitList(filteredSchedules)

            // 필터링된 일정이 없는 경우 로그
            if (filteredSchedules.isEmpty()) {
                timber.log.Timber.w("필터링된 일정이 없습니다. 선택 날짜: $selectedDateFormatted")
            }
        } catch (e: Exception) {
            timber.log.Timber.e("일정 필터링 중 오류: ${e.message}")
            timber.log.Timber.e(e)

            // 오류 발생 시 모든 일정 표시 (디버깅용)
            timber.log.Timber.d("오류로 인해 모든 일정 표시 (${allScheduleItems.size}개)")
            scheduleAdapter.submitList(allScheduleItems)
        }
    }

    // 이번 주 월요일 구하기
    private fun getThisWeekMonday(): Calendar {
        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        val daysToSubtract = when (dayOfWeek) {
            Calendar.SUNDAY -> 6 // 일요일이면 6일 전이 월요일
            else -> dayOfWeek - Calendar.MONDAY // 다른 날은 해당 요일 - 월요일
        }

        return Calendar.getInstance().apply {
            time = today.time
            add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        }
    }

    // TodoItem 데이터 클래스 (API 응답용)
    data class TodoItem(
        val todoId: String,
        val content: String,
        val isDone: Boolean,
        val date: String
    )
}