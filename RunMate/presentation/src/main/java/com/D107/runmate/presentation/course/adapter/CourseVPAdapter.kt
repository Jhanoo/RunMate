package com.D107.runmate.presentation.course.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.D107.runmate.presentation.course.view.CourseFragment

class CourseVPAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return CourseFragment().apply {
            arguments = bundleOf("type" to when (position) {
                0 -> CourseFragment.CourseType.RECENT
                else -> CourseFragment.CourseType.MY
            })
        }
    }
}
