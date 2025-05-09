package com.D107.runmate.presentation.login

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentSplashBinding
import com.ssafy.locket.presentation.base.BaseFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment<FragmentSplashBinding>(
    FragmentSplashBinding::bind,
    R.layout.fragment_splash
) {
    private lateinit var tonieAnimation: AnimationDrawable
    private var navigationJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 애니메이션 설정
        setupFrameAnimation()

        hideMainActivityMenuButton()

        // 3초 후 로그인 화면으로 이동
        navigationJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(3000) // 3초 대기
            if (isAdded && !isDetached) {
                findNavController().navigate(
                    R.id.loginFragment,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true)
                        .build()
                )
            }
        }
    }

    private fun hideMainActivityMenuButton() {
        (activity as? com.D107.runmate.presentation.MainActivity)?.let { mainActivity ->
            mainActivity.findViewById<View>(R.id.btn_menu)?.visibility = View.GONE
        }
    }

    private fun setupFrameAnimation() {
        // 이미지뷰에 배경 설정
        binding.gifImageView.setBackgroundResource(R.drawable.tonie_animation)

        // 배경에서 애니메이션 가져오기
        tonieAnimation = binding.gifImageView.background as AnimationDrawable

        // 애니메이션 파라미터 설정
        tonieAnimation.isOneShot = false  // 반복 애니메이션

        // 애니메이션 시작
        tonieAnimation.start()
    }

    override fun onDestroyView() {
        // 코루틴 Job 취소
        navigationJob?.cancel()
        super.onDestroyView()
    }

    override fun onDestroy() {
        // 애니메이션 정지
        if (::tonieAnimation.isInitialized) {
            tonieAnimation.stop()
        }
        super.onDestroy()
    }
}