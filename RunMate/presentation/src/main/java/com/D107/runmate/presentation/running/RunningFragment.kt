package com.D107.runmate.presentation.running

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.UserLocationState
import com.D107.runmate.presentation.databinding.FragmentRunningBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private const val TAG = "RunningFragment"
@AndroidEntryPoint
class RunningFragment : BaseFragment<FragmentRunningBinding>(
    FragmentRunningBinding::bind,
    R.layout.fragment_running
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()

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
                kakaoMap = p0
            }
        })


        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.userLocation.collectLatest { state ->
                when(state) {
                    is UserLocationState.Exist -> {
                        Log.d(TAG, "onViewCreated: exist ${state.location.latitude} ${state.location.longitude}")
                        val cameraUpdate = CameraUpdateFactory.newCenterPosition(LatLng.from(state.location.latitude, state.location.longitude))
                        kakaoMap?.moveCamera(cameraUpdate)
//                        kakaoMap?.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))
                    }
                    is UserLocationState.Initial -> {
                        Log.d(TAG, "onViewCreated: initial")
                    }
                }
            }
        }



// 1. 애니메이션 효과 없이 바로 지도 이동
//        kakaoMap?.moveCamera(cameraUpdate)


// 2. 애니메이션 효과를 적용하면서 지도 이동
//        kakaoMap?.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))

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