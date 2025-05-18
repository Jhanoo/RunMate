package com.D107.runmate.presentation.course.view

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseSettingBinding
import com.D107.runmate.presentation.course.adapter.CourseVPAdapter
import com.D107.runmate.presentation.utils.CommonUtils.getActivityContext
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseSettingFragment : BaseFragment<FragmentCourseSettingBinding>(
    FragmentCourseSettingBinding::bind,
    R.layout.fragment_course_setting
) {
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabLayout()

        binding.btnBack.setOnClickListener {

            findNavController().popBackStack()
        }

        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_courseSettingFragment_to_courseSearchFragment)
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

    override fun onResume() {
        super.onResume()
        mContext?.let {
            (getActivityContext(it) as MainActivity).hideHamburgerBtn()
        }

    }

}