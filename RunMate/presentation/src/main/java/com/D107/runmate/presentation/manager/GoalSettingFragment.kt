package com.D107.runmate.presentation.manager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGoalSettingBinding
import com.lottiefiles.dotlottie.core.model.Config
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.ssafy.locket.presentation.base.BaseFragment

class GoalSettingFragment : BaseFragment<FragmentGoalSettingBinding>(
    FragmentGoalSettingBinding::bind,
    R.layout.fragment_goal_setting
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.animOnboarding.load(
            Config.Builder()
                .source(DotLottieSource.Asset("manager_onboarding.lottie"))
                .autoplay(true)
                .loop(true)
                .build()
        )
    }
}