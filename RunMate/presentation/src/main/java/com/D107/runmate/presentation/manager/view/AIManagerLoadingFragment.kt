package com.D107.runmate.presentation.manager.view

import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerLoadingBinding
import com.D107.runmate.presentation.manager.viewmodel.CurriculumViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

class AIManagerLoadingFragment : BaseFragment<FragmentAIManagerLoadingBinding>(
    FragmentAIManagerLoadingBinding::bind,
    R.layout.fragment_a_i_manager_loading
) {
    private val curriculumViewModel: CurriculumViewModel by activityViewModels()
    private var animator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAnimations()
        setupBackButton()

        // 30초 후에도 응답이 오지 않으면 강제 이동을 위한 타이머 설정
        viewLifecycleOwner.lifecycleScope.launch {
            delay(10000) // 30초 후
            if (view.isAttachedToWindow && findNavController().currentDestination?.id == R.id.AIManagerLoadingFragment) {
                // 커리큘럼 체크 시도
                curriculumViewModel.getMyCurriculum()
                delay(1000) // API 응답 대기

                // 커리큘럼 ID 확인
                curriculumViewModel.myCurriculum.value?.getOrNull()?.let { curriculum ->
                    Timber.d("타임아웃 후 커리큘럼 발견: ${curriculum.curriculumId}")
                    findNavController().navigate(
                        R.id.action_aiManagerLoading_to_aiManager,
                        bundleOf("curriculumId" to curriculum.curriculumId)
                    )
                } ?: run {
                    // 커리큘럼이 없으면 러닝 화면으로 이동
                    findNavController().navigate(R.id.runningFragment)
                }
            }
        }
    }

    private fun setupBackButton() {
        // 툴바의 뒤로가기 버튼 설정
        binding?.toolbarManagerTitle?.setNavigationOnClickListener {
            // 뒤로가기 버튼 클릭 시 RunningFragment로 이동
            findNavController().navigate(
                R.id.runningFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.AIManagerLoadingFragment, true)
                    .build()
            )
        }
    }

    private fun setupAnimations() {
        binding?.managerLoading?.setImageResource(R.drawable.loading_bg)
        binding?.managerLoading?.scaleType = ImageView.ScaleType.MATRIX

        // 이미지와 뷰의 높이 비율 계산을 위한 변수들
        val imageWidth = resources.getDrawable(R.drawable.loading_forest_bg).intrinsicWidth
        val imageHeight = resources.getDrawable(R.drawable.loading_forest_bg).intrinsicHeight
        val viewHeight = 600f  // 원형 뷰의 높이

        animator = ValueAnimator.ofFloat(0f, 2f).apply {
            duration = 10000 // 10초 동안 이동
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            val bindingRef = WeakReference(binding)

            addUpdateListener { animation ->
                val binding = bindingRef.get() ?: return@addUpdateListener
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
        }

        // ViewTreeObserver를 사용하여 레이아웃이 완료된 후 애니메이션 시작
        binding?.managerLoading?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding?.managerLoading?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                animator?.start()
            }
        })

        // Tonie 애니메이션 설정
        binding?.gifTonie?.setBackgroundResource(R.drawable.tonie_animation)
        (binding?.gifTonie?.background as? AnimationDrawable)?.start()
    }

    override fun onDestroyView() {
        // 애니메이션 정리
        animator?.cancel()
        animator = null
        super.onDestroyView()
    }
}