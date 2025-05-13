package com.D107.runmate.presentation.running.view

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseDetailBinding
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
import com.D107.runmate.presentation.utils.LocationUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.ssafy.locket.presentation.base.BaseFragment
import timber.log.Timber

class CourseDetailFragment : BaseFragment<FragmentCourseDetailBinding>(
    FragmentCourseDetailBinding::bind,
    R.layout.fragment_course_detail
) {
    private var kakaoMap: KakaoMap? = null
//    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: CourseDetailFragmentArgs by navArgs()
        Timber.d("args ${args.courseId}")
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Timber.d("onMapError: " + p0?.message)
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
                addCourseLine(requireContext(), kakaoMap!!, listOf())
                // TODO 세 번째 파라미터 추후 전달받은 코스의 gpx 파일의 좌표값 리스트로 바꾸기
            }

            override fun getPosition(): LatLng {
                val fallbackLocation = LocationUtils.getFallbackLocation()
                return LatLng.from(fallbackLocation.latitude, fallbackLocation.longitude)
            } // TODO 추후 전달받은 코스의 시작지점으로 변경
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