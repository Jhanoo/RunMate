package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentRunningEndBinding
import com.D107.runmate.presentation.utils.LocationUtils
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.ssafy.locket.presentation.base.BaseFragment
import timber.log.Timber

class RunningEndFragment : BaseFragment<FragmentRunningEndBinding>(
    FragmentRunningEndBinding::bind,
    R.layout.fragment_running_end
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Timber.d("onMapError: " + p0?.message)
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
                p0.setOnCameraMoveStartListener { map, gestureType ->
                    if (gestureType == GestureType.Pan) {
//                        map.trackingManager?.stopTracking()
                    }
                }
            }

            override fun getPosition(): LatLng {
                mainViewModel.userLocation.value?.let {
                    if (it is UserLocationState.Exist) {
                        return LatLng.from(
                            it.locations.last().latitude,
                            it.locations.last().longitude
                        )
                    }
                }
                val fallbackLocation = LocationUtils.getFallbackLocation()
                return LatLng.from(fallbackLocation.latitude, fallbackLocation.longitude)
            }
        })

    }
}