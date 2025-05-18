package com.D107.runmate.presentation.manager.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerExpMarathonBinding
import com.D107.runmate.presentation.manager.viewmodel.CurriculumViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AIManagerExpMarathonFragment : BaseFragment<FragmentAIManagerExpMarathonBinding>(
    FragmentAIManagerExpMarathonBinding::bind,
    R.layout.fragment_a_i_manager_exp_marathon
) {
    private val curriculumViewModel: CurriculumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {

        binding.cbMarathonExperience.isChecked = curriculumViewModel.runExp.value

        binding.cbMarathonExperience.setOnCheckedChangeListener { _, isChecked ->
            curriculumViewModel.setRunExp(isChecked)
        }

        binding.rgWeeklyRuns.setOnCheckedChangeListener { _, checkedId ->
            val freqExp = when (checkedId) {
                R.id.rb_run_0 -> "0회"
                R.id.rb_run_1_2 -> "1~2회"
                R.id.rb_run_3_4 -> "3~4회"
                R.id.rb_run_5_more -> "5회 이상"
                else -> "1~2회"
            }
            curriculumViewModel.setFreqExp(freqExp)
        }

        binding.rgMaxDistance.setOnCheckedChangeListener { _, checkedId ->
            val distExp = when (checkedId) {
                R.id.rb_distance_5km -> "~5km"
                R.id.rb_distance_10km -> "~10km"
                R.id.rb_distance_half -> "~하프"
                R.id.rb_distance_full -> "~풀"
                else -> "~10km"
            }
            curriculumViewModel.setDistExp(distExp)
        }

        binding.btnConfirmExp.setOnClickListener {
            findNavController().navigate(R.id.action_aiManagerExp_to_aiManagerGoal)
        }
    }
}