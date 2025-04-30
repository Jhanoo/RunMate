package com.D107.runmate.presentation.running

import android.os.Bundle
import android.util.Log
import android.view.View
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentRunningBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapReadyCallback
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

private const val TAG = "RunningFragment"
@AndroidEntryPoint
class RunningFragment : BaseFragment<FragmentRunningBinding>(
    FragmentRunningBinding::bind,
    R.layout.fragment_running
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d(TAG, "onMapDestroy: ")
            }

            override fun onMapError(p0: Exception?) {
                Log.d(TAG, "onMapError: ${p0?.message}")
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                Log.d(TAG, "onMapReady: ")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }
}