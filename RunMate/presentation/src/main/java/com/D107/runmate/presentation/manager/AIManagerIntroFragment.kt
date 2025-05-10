package com.D107.runmate.presentation.manager

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerIntroBinding
import com.ssafy.locket.presentation.base.BaseFragment

class AIManagerIntroFragment : BaseFragment<FragmentAIManagerIntroBinding>(
    FragmentAIManagerIntroBinding::bind,
    R.layout.fragment_a_i_manager_intro
){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWithBoldSpan()
    }

    private fun setupTextWithBoldSpan() {
        val text = "AI 매니저가 달리기 수준, 목표, 마라톤 일정에 맞춘 맞춤형 러닝 플랜을 제공합니다. 마라톤 준비부터 일상 러닝까지, 최적의 훈련 스케줄을 완료하며 체계적으로 러닝 실력을 향상시켜 보세요. AI 매니저가 부상 없이 목표 달성까지 함께합니다."
        val spannableString = SpannableString(text)

        // 굵게 표시할 텍스트
        val boldText = "달리기 수준, 목표, 마라톤 일정"
        val boldStart = text.indexOf(boldText)
        val boldEnd = boldStart + boldText.length

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            boldStart,
            boldEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textView.text = spannableString
    }

}