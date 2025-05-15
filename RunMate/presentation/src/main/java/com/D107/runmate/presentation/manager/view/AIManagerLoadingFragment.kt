package com.D107.runmate.presentation.manager.view

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerLoadingBinding
import com.ssafy.locket.presentation.base.BaseFragment

class AIManagerLoadingFragment : BaseFragment<FragmentAIManagerLoadingBinding>(
    FragmentAIManagerLoadingBinding::bind,
    R.layout.fragment_a_i_manager_loading
){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.managerLoading.setImageResource(R.drawable.loading_bg)
        binding.managerLoading.scaleType = ImageView.ScaleType.MATRIX

        // 이미지와 뷰의 높이 비율 계산을 위한 변수들
        val imageWidth = resources.getDrawable(R.drawable.loading_forest_bg).intrinsicWidth
        val imageHeight = resources.getDrawable(R.drawable.loading_forest_bg).intrinsicHeight
        val viewHeight = 600f  // 원형 뷰의 높이

        val animator = ValueAnimator.ofFloat(0f, 2f)
        animator.duration = 10000 // 10초 동안 이동
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val matrix = Matrix()
            val value = animation.animatedValue as Float

            // 이미지 스케일 계산 (이미지의 높이를 뷰의 높이에 맞춤)
            val scale = viewHeight / imageHeight
            matrix.postScale(scale, scale)

            // 스케일링된 이미지 너비 계산
            val scaledImageWidth = imageWidth * scale

            // 이미지의 세로 중앙을 뷰의 세로 중앙에 맞추기
            matrix.postTranslate(0f, 0f)

            // 이미지 가로 애니메이션
            val maxOffset = scaledImageWidth - viewHeight  // 원형이므로 너비와 높이가 같음
            val translateX = -value * maxOffset
            matrix.postTranslate(translateX, 0f)

            binding.managerLoading.imageMatrix = matrix
        }

        // ViewTreeObserver를 사용하여 레이아웃이 완료된 후 애니메이션 시작
        binding.managerLoading.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.managerLoading.viewTreeObserver.removeOnGlobalLayoutListener(this)
                animator.start()
            }
        })

        // Tonie 애니메이션 설정
        binding.gifTonie.setBackgroundResource(R.drawable.tonie_animation)
        val tonieAnimation = binding.gifTonie.background as AnimationDrawable
        tonieAnimation.start()
    }
}