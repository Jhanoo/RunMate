package com.D107.runmate.presentation.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentSplashBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ssafy.locket.presentation.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashFragment : BaseFragment<FragmentSplashBinding>(
    FragmentSplashBinding::bind,
    R.layout.fragment_splash
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        lifecycleScope.launch(Dispatchers.IO) {
//            // 백그라운드에서 로드
//            val gifDrawable = Glide.with(this@SplashFragment)
//                .asGif()
//                .load(R.raw.tonie)
//                .submit()
//                .get()
//
//            // UI 스레드에서 이미지뷰에 설정
//            withContext(Dispatchers.Main) {
//                binding.gifImageView.setImageDrawable(gifDrawable)
//            }
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .asGif()
            .load(R.raw.tonie)
//            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.gifImageView)

//        Glide.with(this)
//            .asGif()
//            .load(R.raw.tonie)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .into(binding.gifImageView)

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }, 3000)

    }
}