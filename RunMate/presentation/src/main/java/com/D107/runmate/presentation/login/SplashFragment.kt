package com.D107.runmate.presentation.login

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentSplashBinding
import com.ssafy.locket.presentation.base.BaseFragment

class SplashFragment : BaseFragment<FragmentSplashBinding>(
    FragmentSplashBinding::bind,
    R.layout.fragment_splash
) {
    private lateinit var tonieAnimation: AnimationDrawable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 애니메이션 설정
        setupFrameAnimation()

        // 3초 후 로그인 화면으로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && !isDetached) {
                findNavController().navigate(
                    R.id.loginFragment,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true)
                        .build()
                )
            }
        }, 3000)
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

    override fun onDestroy() {
        // 애니메이션 정지
        if (::tonieAnimation.isInitialized) {
            tonieAnimation.stop()
        }
        super.onDestroy()
    }
}