package com.D107.runmate.presentation.running.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentChartBinding
import com.ssafy.locket.presentation.base.BaseFragment

class ChartFragment : BaseFragment<FragmentChartBinding>(
    FragmentChartBinding::bind,
    R.layout.fragment_chart
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }
}