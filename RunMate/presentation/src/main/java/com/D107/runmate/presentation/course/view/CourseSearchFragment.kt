package com.D107.runmate.presentation.course.view

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseSearchBinding
import com.D107.runmate.presentation.running.CourseSearchState
import com.D107.runmate.presentation.running.CourseViewModel
import com.D107.runmate.presentation.course.adapter.CourseRVAdapter
import com.D107.runmate.presentation.history.view.FilterDialog
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class CourseSearchFragment : BaseFragment<FragmentCourseSearchBinding>(
    FragmentCourseSearchBinding::bind,
    R.layout.fragment_course_search
) {
    private lateinit var dialog: FilterDialog
    private var setting: List<Int?> = listOf(null, null) // 첫 번째 거는 선택된 아이템(거리)의 index(선택 x는 -1), 두 번째 거는 0, 1로 bool대신 int
    private lateinit var courseRVAdapter: CourseRVAdapter
    private val courseViewModel: CourseViewModel by viewModels()
    private val distanceFilterList = listOf(0.0, 5.0, 10.0, 21.097, 120.0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        courseViewModel.getAllCourseList()

        binding.btnClear.setOnClickListener {
            binding.etSearch.setText("")
            courseViewModel.getAllCourseList()
            binding.btnClear.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFilter.setOnClickListener {
            dialog = FilterDialog(0, setting) { value ->
                if (value.size != 0) {
                    val isLiked = if (value[1] == 0) false else if (value[1] == 1) true else null

                    val courseList = courseViewModel.courseList.value
                    if (courseList is CourseSearchState.Success) {
                        val minDistance =
                            if (value[0] == null) null else distanceFilterList[value[0]!!]
                        val maxDistance =
                            if (value[0] == null) null else if (value[0] == distanceFilterList.size - 1) null else distanceFilterList[value[0]!! + 1]
                        if (isLiked == null) {
                            if (minDistance == null && maxDistance == null) {
                                courseRVAdapter.submitList(courseList.courseList)
                            } else if (minDistance != null && maxDistance == null) {
                                courseRVAdapter.submitList(
                                    courseList.courseList.filter {
                                        it.distance > minDistance
                                    }
                                )
                            } else {
                                courseRVAdapter.submitList(
                                    courseList.courseList.filter {
                                        it.distance >= minDistance!! && it.distance < maxDistance!!
                                    }
                                )
                            }
                        } else {
                            if (minDistance == null && maxDistance == null) {
                                courseRVAdapter.submitList(courseList.courseList.filter { it.liked == isLiked })
                            } else if (minDistance != null && maxDistance == null) {
                                courseRVAdapter.submitList(
                                    courseList.courseList.filter {
                                        it.distance > minDistance && it.liked == isLiked
                                    }
                                )
                            } else {
                                courseRVAdapter.submitList(
                                    courseList.courseList.filter {
                                        it.distance >= minDistance!! && it.distance < maxDistance!! && it.liked == isLiked
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Timber.d("onViewCreated: value is empty")
                }
                setting = value
            }
            dialog.show(requireActivity().supportFragmentManager, "filter")
        }

        binding.etSearch.setOnEditorActionListener{ v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if(query.isNotEmpty()) {
                    binding.btnClear.visibility = View.VISIBLE
                    courseViewModel.searchCourse(query)
                }
                return@setOnEditorActionListener true
            } else {
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.courseList.collectLatest { state ->
                when (state) {
                    is CourseSearchState.Success -> {
                        courseRVAdapter.submitList(state.courseList)
                    }
                    is CourseSearchState.Error -> {
                        Timber.d("getAllCourseList Error {${state.message}}")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun initAdapter() {
        courseRVAdapter = CourseRVAdapter()

        binding.rvCourse.apply {
            adapter = courseRVAdapter
            layoutManager = LinearLayoutManager(requireContext())
            courseRVAdapter.submitList(listOf())
        }

        courseRVAdapter.itemClickListener = object : CourseRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: CourseInfo, position: Int) {
                val action = CourseSearchFragmentDirections.actionCourseSearchFragmentToCourseDetailFragment(data.courseId)
                findNavController().navigate(action)
            }
        }
    }
}