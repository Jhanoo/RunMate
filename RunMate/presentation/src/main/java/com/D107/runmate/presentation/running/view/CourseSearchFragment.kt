package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.domain.model.course.CourseFilter
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.course.Creator
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseSearchBinding
import com.D107.runmate.presentation.running.CourseFilterState
import com.D107.runmate.presentation.running.CourseSearchState
import com.D107.runmate.presentation.running.CourseViewModel
import com.D107.runmate.presentation.running.adapter.CourseRVAdapter
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max

@AndroidEntryPoint
class CourseSearchFragment : BaseFragment<FragmentCourseSearchBinding>(
    FragmentCourseSearchBinding::bind,
    R.layout.fragment_course_search
) {
    private lateinit var dialog: FilterDialog
    private var setting: List<Int?> = listOf(null, null) // 첫 번째 거는 선택된 아이템의 index(선택 x는 -1), 두 번째 거는 0, 1로 bool대신 int
    private lateinit var courseRVAdapter: CourseRVAdapter
    private val courseViewModel: CourseViewModel by viewModels()
    private val distanceFilterList = listOf(0.0, 5.0, 10.0, 21.097, 120.0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        binding.btnFilter.setOnClickListener {
            dialog = FilterDialog(setting) { value ->
                if (value.size != 0) {
                    Timber.d("onViewCreated: value ${value[0]} ${value[1]}")
                    val isLiked = if(value[1] == 0) false else if(value[1] == 1) true else null

//                    courseViewModel.setCourseFilter(CourseFilterState.Set(CourseFilter(isLiked, maxDistance)))
                    val courseList = courseViewModel.courseList.value
                    if(courseList is CourseSearchState.Success) {
                        // TODO isLiked가 null인 경우, liked 빼고 filter
                        // 0이면 0초과 5.0이하, 1이면 5.0초과 10.0이하, 2이면 10.0초과 21.097이하, 3이면 21.097초과, -1이나 else면 null로 filter x
                        val minDistance = if(value[0] == null) null else distanceFilterList[value[0]!!]
                        val maxDistance = if(value[0] == null) null else if(value[0] == distanceFilterList.size-1) null else distanceFilterList[value[0]!!+1]
                        if(isLiked == null){
                            if(minDistance == null && maxDistance == null) {
                                courseRVAdapter.submitList(courseList.courseList)
                            } else if(minDistance != null && maxDistance == null) {
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
                            if(minDistance == null && maxDistance == null) {
                                courseRVAdapter.submitList(courseList.courseList.filter { it.liked == isLiked })
                            } else if(minDistance != null && maxDistance == null) {
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
//                        courseRVAdapter.submitList(
//                            courseList.courseList.filter {
//                                it.distance <= maxDistance && it.isLiked == isLiked
//                            }
//                        )
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
                        Timber.d("getAllCourseList Success {${state.courseList}}")
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

//        val tmpList = listOf(
//            CourseInfo(0.1, "0", "테스트", Creator("테스트유저", "https://picsum.photos/200/300"), 1.2,13, true, false, "경상북도 구미시" ),
//            CourseInfo(0.6, "12", "테스트1", Creator("테스트유저", "https://picsum.photos/200/300"), 6.2,190, true, true, "경상북도 구미시" ),
//            CourseInfo(0.9, "443", "테스트44444", Creator("테스트유저", "https://picsum.photos/200/300"), 1.2,13, true, true, "경상북도 구미시" ))

        binding.rvCourse.apply {
            adapter = courseRVAdapter
            layoutManager = LinearLayoutManager(requireContext())
            courseRVAdapter.submitList(listOf())
        }


        courseRVAdapter.itemClickListener = object : CourseRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: CourseInfo, position: Int) {
                Timber.d("onClick: ${data.courseId} ${data.courseName}")
                val action = CourseSearchFragmentDirections.actionCourseSearchFragmentToCourseDetailFragment(data.courseId)
                findNavController().navigate(action)
            }
        }
    }
}