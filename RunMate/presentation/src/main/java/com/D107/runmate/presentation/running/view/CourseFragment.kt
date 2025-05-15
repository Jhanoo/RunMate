package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.course.Creator
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseBinding
import com.D107.runmate.presentation.running.CourseSearchState
import com.D107.runmate.presentation.running.CourseViewModel
import com.D107.runmate.presentation.running.adapter.CourseRVAdapter
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class CourseFragment : BaseFragment<FragmentCourseBinding>(
    FragmentCourseBinding::bind,
    R.layout.fragment_course
) {
    enum class CourseType { RECENT, MY }

    private var type: CourseType = CourseType.RECENT
    private lateinit var courseRVAdapter: CourseRVAdapter
    private val mainViewModel: MainViewModel by activityViewModels()
    private val courseViewModel: CourseViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        type = arguments?.getSerializable("type") as CourseType? ?: CourseType.RECENT

        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.myCourseList.collect { state ->
                when (state) {
                    is CourseSearchState.Success -> {
                        courseRVAdapter.submitList(state.courseList)
                    }

                    is CourseSearchState.Error -> {
                        Timber.d("getAllCourseList Error {${state.message}}")
                    }

                    CourseSearchState.Initial -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.recentCourseList.collect { state ->
                when (state) {
                    is CourseSearchState.Success -> {
                        courseRVAdapter.submitList(state.courseList)
                    }

                    is CourseSearchState.Error -> {
                        Timber.d("getAllCourseList Error {${state.message}}")
                    }

                    CourseSearchState.Initial -> {}
                }
            }
        }

        when (type) {
            CourseType.RECENT -> {
                // TODO 최근 코스 조회
                Timber.d("최근 코스")

                courseRVAdapter = CourseRVAdapter()

                binding.rvCourse.apply {
                    adapter = courseRVAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    courseRVAdapter.submitList(listOf())
                }
                courseViewModel.getRecentCourse()

                courseRVAdapter.itemClickListener = object : CourseRVAdapter.ItemClickListener {
                    override fun onClick(view: View, data: CourseInfo, position: Int) {
                        Timber.d("onClick: ${data.courseId} ${data.courseName}")
                        val actions =
                            CourseSettingFragmentDirections.actionCourseSettingFragmentToCourseDetailFragment(
                                data.courseId
                            )
                        findNavController().navigate(actions)
                    }
                }
            }

            CourseType.MY -> {
                // TODO 내가 만든 코스 조회
                Timber.d("내가 만든 코스")

                courseRVAdapter = CourseRVAdapter()

                binding.rvCourse.apply {
                    adapter = courseRVAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    courseRVAdapter.submitList(listOf())
                }
                courseViewModel.getMyCourse()

                courseRVAdapter.itemClickListener = object : CourseRVAdapter.ItemClickListener {
                    override fun onClick(view: View, data: CourseInfo, position: Int) {
                        Timber.d("onClick: ${data.courseId} ${data.courseName}")
                        val actions =
                            CourseSettingFragmentDirections.actionCourseSettingFragmentToCourseDetailFragment(
                                data.courseId
                            )
                        findNavController().navigate(actions)
                    }
                }
            }
        }
    }
}
