package com.D107.runmate.presentation.user.view

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentSplashBinding
import com.D107.runmate.presentation.user.viewmodel.SplashViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(
    FragmentSplashBinding::bind,
    R.layout.fragment_splash
) {
    private lateinit var tonieAnimation: AnimationDrawable
    private var navigationJob: Job? = null
    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFrameAnimation()
        hideMainActivityMenuButton()
        observeLoginStatus()
    }

    private fun observeLoginStatus() {
        navigationJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) // 2초 대기 (애니메이션 보여주기 위한 최소 시간)

            viewModel.isLoggedIn.collectLatest { isLoggedIn ->
                if (isAdded && !isDetached) {
                    if (isLoggedIn) {
                        // 로그인된 상태면 메인 화면으로 이동
                        findNavController().navigate(
                            R.id.runningFragment,
                            null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.splashFragment, true)
                                .build()
                        )
                    } else {
                        // 로그인되지 않은 상태면 로그인 화면으로 이동
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
        }
    }

    private fun hideMainActivityMenuButton() {
        (activity as? com.D107.runmate.presentation.MainActivity)?.let { mainActivity ->
            mainActivity.findViewById<View>(R.id.btn_menu)?.visibility = View.GONE
        }
    }

    private fun setupFrameAnimation() {
        binding.gifImageView.setBackgroundResource(R.drawable.tonie_animation)
        tonieAnimation = binding.gifImageView.background as AnimationDrawable
        tonieAnimation.isOneShot = false
        tonieAnimation.start()
    }

    override fun onDestroyView() {
        navigationJob?.cancel()
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (::tonieAnimation.isInitialized) {
            tonieAnimation.stop()
        }
        super.onDestroy()
    }
}