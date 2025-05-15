package com.D107.runmate.presentation.manager.view

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerExpMarathonBinding
import com.ssafy.locket.presentation.base.BaseFragment

class AIManagerExpMarathonFragment : BaseFragment<FragmentAIManagerExpMarathonBinding>(
    FragmentAIManagerExpMarathonBinding::bind,
    R.layout.fragment_a_i_manager_exp_marathon
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnConfirmExp.setOnClickListener {
            findNavController().navigate(R.id.action_aiManagerExp_to_aiManagerGoal)
        }
    }
}