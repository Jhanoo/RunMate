package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.running.CourseInfo
import com.D107.runmate.domain.model.running.Creator
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseSearchBinding
import com.D107.runmate.presentation.running.adapter.CourseRVAdapter
import com.ssafy.locket.presentation.base.BaseFragment
import timber.log.Timber

class CourseSearchFragment : BaseFragment<FragmentCourseSearchBinding>(
    FragmentCourseSearchBinding::bind,
    R.layout.fragment_course_search
) {
    private lateinit var dialog: FilterDialog
    private var setting: List<Int> = listOf(-1, 0) // 첫 번째 거는 선택된 아이템의 index(선택 x는 -1), 두 번째 거는 0, 1로 bool대신 int
    private lateinit var courseRVAdapter: CourseRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        binding.btnFilter.setOnClickListener {
            dialog = FilterDialog(setting) { value ->
                if (value.size != 0) {
                    Timber.d("onViewCreated: value ${value[0]} ${value[1]}")
                } else {
                    Timber.d("onViewCreated: value is empty")
                }
                setting = value
            }
            dialog.show(requireActivity().supportFragmentManager, "filter")
        }
    }

    private fun initAdapter() {
        courseRVAdapter = CourseRVAdapter()

        val tmpList = listOf(CourseInfo(0.1, "0", "테스트", Creator("테스트유저", "https://picsum.photos/200/300"), 1.2,13, true, false, "경상북도 구미시" ),
            CourseInfo(0.6, "12", "테스트1", Creator("테스트유저", "https://picsum.photos/200/300"), 6.2,190, true, true, "경상북도 구미시" ),
            CourseInfo(0.9, "443", "테스트44444", Creator("테스트유저", "https://picsum.photos/200/300"), 1.2,13, true, true, "경상북도 구미시" ))

        binding.rvCourse.apply {
            adapter = courseRVAdapter
            layoutManager = LinearLayoutManager(requireContext())
            courseRVAdapter.submitList(tmpList)
        }


        courseRVAdapter.itemClickListener = object : CourseRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: CourseInfo, position: Int) {
                // TODO 코스 상세 화면으로 이동
                Timber.d("onClick: ${data.courseId} ${data.courseName}")
            }
        }
    }
}