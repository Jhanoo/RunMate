package com.D107.runmate.presentation.manager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerBinding
import com.ssafy.locket.presentation.base.BaseFragment

class AIManagerFragment : BaseFragment<FragmentAIManagerBinding>(
    FragmentAIManagerBinding::bind,
    R.layout.fragment_a_i_manager
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calendar.setTitleFormatter { day ->
            val year = day.year
            val month = day.month + 1 // month는 0부터 시작하므로 +1
            "%04d.%02d".format(year, month)
        }

    }
}