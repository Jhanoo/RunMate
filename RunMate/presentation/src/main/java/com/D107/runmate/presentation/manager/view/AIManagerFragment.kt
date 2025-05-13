package com.D107.runmate.presentation.manager.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.D107.runmate.domain.model.manager.ScheduleItem
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerBinding
import com.D107.runmate.presentation.manager.adapter.ScheduleRVAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.ssafy.locket.presentation.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AIManagerFragment : BaseFragment<FragmentAIManagerBinding>(
    FragmentAIManagerBinding::bind,
    R.layout.fragment_a_i_manager
) {
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

            // 오늘 날짜를 포함한 주의 월요일부터의 날짜를 계산
            val thisWeekMonday = getThisWeekMonday()

            // 선택한 날짜가 이번 주 월요일보다 이전이거나 같고, 오늘보다 이전이거나 같은 경우만 처리
            if (!selectedCalendar.after(Calendar.getInstance()) && !selectedCalendar.before(
                    thisWeekMonday
                )
            ) {
                loadSchedulesForSelectedDate(selectedCalendar)
            } else if (selectedCalendar.after(Calendar.getInstance())) {
                Toast.makeText(context, "미래 날짜는 선택할 수 없습니다", Toast.LENGTH_SHORT).show()
                binding.calendar.setDateSelected(today, true)
            }
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
        // 예시 코드: 실제 환경에서는 ViewModel 및 Repository 패턴 사용을 권장
        lifecycleScope.launch {
            try {
                // 네트워크 호출은 백그라운드 스레드에서 실행
                withContext(Dispatchers.IO) {
                    // 실제 API 호출 부분 (예시)
                    /*
                    apiService.getTodos(year, month).enqueue(object : Callback<TodoResponse> {
                        override fun onResponse(call: Call<TodoResponse>, response: Response<TodoResponse>) {
                            if (response.isSuccessful) {
                                val todoResponse = response.body()
                                todoResponse?.data?.let { todos ->
                                    // API 응답을 ScheduleItem 리스트로 변환
                                    allScheduleItems.clear()
                                    allScheduleItems.addAll(convertToScheduleItems(todos))

                                    // 선택한 날짜에 해당하는 일정만 필터링하여 표시
                                    filterAndDisplaySchedules(selectedDate)
                                }
                            } else {
                                Toast.makeText(context, "일정을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<TodoResponse>, t: Throwable) {
                            Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    })
                    */

                    // 샘플 데이터로 테스트 (실제 구현 시 API 호출로 대체)
                    allScheduleItems.clear()
                    allScheduleItems.add(
                        ScheduleItem(
                            "5/10", "토", "5km 속도 연습", false,
                            Color.parseColor("#4CAF50"),
                            "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                        )
                    )

                    // 더 많은 샘플 데이터 추가 (오늘 날짜와 주변 날짜)
                    val today = Calendar.getInstance()
                    val dayFormat = SimpleDateFormat("M/d", Locale.getDefault())
                    val weekDayFormat = SimpleDateFormat("E", Locale.KOREAN)

                    for (i in -2..4) {
                        val date = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_MONTH, i)
                        }

                        val dateStr = dayFormat.format(date.time)
                        val dayStr = weekDayFormat.format(date.time)

                        allScheduleItems.add(
                            ScheduleItem(
                                dateStr,
                                dayStr,
                                "러닝 ${5 + i}km",
                                false,
                                when (date.get(Calendar.DAY_OF_WEEK)) {
                                    Calendar.MONDAY -> Color.parseColor("#FF5722")
                                    Calendar.WEDNESDAY -> Color.parseColor("#2196F3")
                                    Calendar.FRIDAY -> Color.parseColor("#9C27B0")
                                    else -> Color.parseColor("#4CAF50")
                                },
                                java.util.UUID.randomUUID().toString()
                            )
                        )
                    }
                }

                // UI 업데이트는 메인 스레드에서 실행
                filterAndDisplaySchedules(selectedDate)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // API 응답을 ScheduleItem 리스트로 변환
    private fun convertToScheduleItems(todos: List<TodoItem>): List<ScheduleItem> {
        return todos.map { todo ->
            // 날짜 포맷 변환 (ISO-8601 -> M/d)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val date = dateFormat.parse(todo.date)

            val outputFormat = SimpleDateFormat("M/d", Locale.getDefault())
            val dayFormat = SimpleDateFormat("E", Locale.KOREAN)

            ScheduleItem(
                outputFormat.format(date!!),
                dayFormat.format(date),
                todo.content,
                todo.isDone,
                Color.parseColor("#4CAF50"),
                todo.todoId
            )
        }
    }

    // 선택한 날짜에 해당하는 일정만 필터링하여 표시
    private fun filterAndDisplaySchedules(selectedDate: Calendar) {

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