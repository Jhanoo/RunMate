package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.view.View
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseBinding
import com.ssafy.locket.presentation.base.BaseFragment

class CourseFragment : BaseFragment<FragmentCourseBinding>(
    FragmentCourseBinding::bind,
    R.layout.fragment_course
) {
    enum class CourseType { RECENT, MY }

    private var type: CourseType = CourseType.RECENT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (type) {
            CourseType.RECENT -> {

            }
            CourseType.MY -> {

            }
        }
    }
}
