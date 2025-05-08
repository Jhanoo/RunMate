package com.D107.runmate.presentation.running.view

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseSettingBinding
import com.D107.runmate.presentation.running.adapter.CourseVPAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ssafy.locket.presentation.base.BaseFragment
import kotlinx.coroutines.launch

class CourseSettingFragment : BaseFragment<FragmentCourseSettingBinding>(
    FragmentCourseSettingBinding::bind,
    R.layout.fragment_course_setting
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabLayout()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSearch.setOnClickListener {
            // TODO 검색 화면으로 이동
        }
    }

    private fun initTabLayout() {
        binding.tabLayout.apply {
            addTab(binding.tabLayout.newTab().setText("최근에 달린 코스"))
            addTab(binding.tabLayout.newTab().setText("내가 만든 코스"))
        }

        binding.tabVp.apply {
            adapter = CourseVPAdapter(requireActivity() as MainActivity)
            isUserInputEnabled = false
        }

        TabLayoutMediator(binding.tabLayout, binding.tabVp) { tab, position ->
            tab.text = if (position == 0) "최근에 달린 코스" else "내가 만든 코스"
        }.attach()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabTextView = getTextViewFromTab(tab)
                tabTextView?.setTypeface(null, Typeface.NORMAL)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabTextView = getTextViewFromTab(tab)
                tabTextView?.setTypeface(null, Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                val tabTextView = getTextViewFromTab(tab)
                tabTextView?.setTypeface(null, Typeface.NORMAL)
            }
        })
    }

    private fun getTextViewFromTab(tab: TabLayout.Tab): TextView? {
        val tabLayout = tab.parent as TabLayout
        val tabStrip = tabLayout.getChildAt(0) as ViewGroup
        val tabView = tabStrip.getChildAt(tab.position) as ViewGroup

        for (i in 0 until tabView.childCount) {
            val child = tabView.getChildAt(i)
            if (child is TextView) {
                return child
            }
        }
        return null
    }

}