package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.running.CourseInfo
import com.D107.runmate.domain.model.running.Creator
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseBinding
import com.D107.runmate.presentation.running.adapter.CourseRVAdapter
import com.ssafy.locket.presentation.base.BaseFragment
import timber.log.Timber

class CourseFragment : BaseFragment<FragmentCourseBinding>(
    FragmentCourseBinding::bind,
    R.layout.fragment_course
) {
    enum class CourseType { RECENT, MY }

    private var type: CourseType = CourseType.RECENT
    private lateinit var courseRVAdapter: CourseRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tmpList = listOf(
            CourseInfo(0.1, "0", "테스트", Creator("테스트유저", "https://picsum.photos/200/300"), 1.2,13, true, false, "경상북도 구미시" ),
            CourseInfo(0.6, "12", "테스트1", Creator("테스트유저", "https://picsum.photos/200/300"), 6.2,190, true, true, "경상북도 구미시" ),
            CourseInfo(0.9, "443", "테스트44444", Creator("테스트유저", "https://picsum.photos/200/300"), 1.2,13, true, true, "경상북도 구미시" )
        )

        when (type) {
            CourseType.RECENT -> {
                // TODO 최근 코스 조회
                courseRVAdapter = CourseRVAdapter()

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
            CourseType.MY -> {
                // TODO 내가 만든 코스 조회
                courseRVAdapter = CourseRVAdapter()

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
    }
}
