package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.TrackingStatus
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
    private lateinit var dialog: CourseAddDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initEvent()
        initMap()

    }

    private fun initEvent() {
        binding.btnNext.setOnClickListener {
            mainViewModel.setTrackingStatus(TrackingStatus.INITIAL)
            findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
        }

        binding.btnChart.setOnClickListener {
            findNavController().navigate(R.id.action_runningEndFragment_to_chartFragment)
        }

        binding.btnAddCourse.setOnClickListener {
            dialog = CourseAddDialog()
            dialog.show(requireActivity().supportFragmentManager, "course_add")
        }
    }

    private fun initMap() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Timber.d("onMapError: " + p0?.message)
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
            }

            override fun getPosition(): LatLng {
                // TODO 코스의 시작지점을 LatLng으로 반환
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

    private fun initUI() {
        // TODO 프리 모드인 경우
        binding.btnAddCourse.visibility = View.VISIBLE
        binding.ivLike.visibility = View.GONE

        // TODO 코스 모드인 경우
//        binding.btnAddCourse.visibility = View.GONE
//        binding.ivLike.visibility = View.VISIBLE

        // TODO 코스 모드인 경우, 사용자 좋아요 여부 좋아요 x
//        binding.ivLike.setImageResource(R.drawable.ic_course_like_inactive)

        // TODO 코스 모드인 경우, 사용자 좋아요 여부 좋아요 o
//        binding.ivLike.setImageResource(R.drawable.ic_course_like)
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