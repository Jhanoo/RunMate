package com.D107.runmate.presentation.manager.view

import android.os.Bundle
import android.view.View
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