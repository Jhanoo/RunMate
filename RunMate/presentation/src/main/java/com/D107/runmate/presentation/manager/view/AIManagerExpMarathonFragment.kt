package com.D107.runmate.presentation.manager.view

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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
        setupInitialState()
    }

    private fun setupInitialState() {
        // 체크박스 초기 상태 설정
        binding.cbMarathonExperience.isChecked = curriculumViewModel.runExp.value

        // 주간 달리기 횟수 초기 상태 설정
        when (curriculumViewModel.freqExp.value) {
            "0회" -> binding.rbRun0.isChecked = true
            "1~2회" -> binding.rbRun12.isChecked = true
            "3~4회" -> binding.rbRun34.isChecked = true
            "5회 이상" -> binding.rbRun5More.isChecked = true
            else -> binding.rbRun12.isChecked = true
        }

        // 라디오 버튼 텍스트 색상 설정
        setFreqRadioTextColors()

        // 최대 달리기 거리 초기 상태 설정
        when (curriculumViewModel.distExp.value) {
            "~5km" -> binding.rbDistance5km.isChecked = true
            "~10km" -> binding.rbDistance10km.isChecked = true
            "~하프" -> binding.rbDistanceHalf.isChecked = true
            "~풀" -> binding.rbDistanceFull.isChecked = true
            else -> binding.rbDistance10km.isChecked = true
        }

        // 거리 라디오 버튼 텍스트 색상 설정
        setDistRadioTextColors()
    }

    private fun setupListeners() {
        binding.toolbarExpTitle.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

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

            // 텍스트 색상 업데이트
            setFreqRadioTextColors()
        }

        binding.rgMaxDistance.setOnCheckedChangeListener { _, checkedId ->
            val distExp = when (checkedId) {
                R.id.rb_distance_5km -> "5km"
                R.id.rb_distance_10km -> "10km"
                R.id.rb_distance_half -> "하프"
                R.id.rb_distance_full -> "풀"
                else -> "~10km"
            }
            curriculumViewModel.setDistExp(distExp)

            // 텍스트 색상 업데이트
            setDistRadioTextColors()
        }

        binding.btnConfirmExp.setOnClickListener {
            findNavController().navigate(R.id.action_aiManagerExp_to_aiManagerGoal)
        }
    }

    private fun setFreqRadioTextColors() {
        // 주간 달리기 횟수 라디오 버튼 텍스트 색상 설정
        binding.rbRun0.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbRun0.isChecked) android.R.color.white else android.R.color.black
            )
        )
        binding.rbRun12.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbRun12.isChecked) android.R.color.white else android.R.color.black
            )
        )
        binding.rbRun34.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbRun34.isChecked) android.R.color.white else android.R.color.black
            )
        )
        binding.rbRun5More.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbRun5More.isChecked) android.R.color.white else android.R.color.black
            )
        )
    }

    private fun setDistRadioTextColors() {
        // 최대 달리기 거리 라디오 버튼 텍스트 색상 설정
        binding.rbDistance5km.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbDistance5km.isChecked) android.R.color.white else android.R.color.black
            )
        )
        binding.rbDistance10km.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbDistance10km.isChecked) android.R.color.white else android.R.color.black
            )
        )
        binding.rbDistanceHalf.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbDistanceHalf.isChecked) android.R.color.white else android.R.color.black
            )
        )
        binding.rbDistanceFull.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (binding.rbDistanceFull.isChecked) android.R.color.white else android.R.color.black
            )
        )
    }
}