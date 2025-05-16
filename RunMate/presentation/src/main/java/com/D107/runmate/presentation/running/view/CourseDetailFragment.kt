package com.D107.runmate.presentation.running.view

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentCourseDetailBinding
import com.D107.runmate.presentation.running.CourseDetailState
import com.D107.runmate.presentation.running.CourseViewModel
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToHMS
import com.D107.runmate.presentation.utils.GpxParser.downloadFile
import com.D107.runmate.presentation.utils.GpxParser.getGpxInputStream
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
import com.D107.runmate.presentation.utils.LocationUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

@AndroidEntryPoint
class CourseDetailFragment : BaseFragment<FragmentCourseDetailBinding>(
    FragmentCourseDetailBinding::bind,
    R.layout.fragment_course_detail
) {
    private var kakaoMap: KakaoMap? = null
    private val courseViewModel: CourseViewModel by viewModels()
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: CourseDetailFragmentArgs by navArgs()

        courseViewModel.getCourseDetail(args.courseId)

        binding.btnNext.setOnClickListener {

        }

        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.courseDetail.collectLatest {
                if (it is CourseDetailState.Success) {
                    binding.tvCourseTitle.text = it.courseDetail.name
                    binding.tvCourseDistance.text =
                        getString(R.string.course_distance, it.courseDetail.distance)
                    binding.tvCourseLikeCnt.text = it.courseDetail.likes.toString()
                    binding.tvAvgAltitude.text =
                        getString(R.string.course_altitude, it.courseDetail.avgElevation)
                    binding.tvAvgDuration.text =
                        if (it.courseDetail.avgEstimatedTime == null) "-" else formatSecondsToHMS(it.courseDetail.avgEstimatedTime!!)
                    binding.tvExpDuration.text =
                        if (it.courseDetail.userEstimatedTime == null) "-" else formatSecondsToHMS(
                            it.courseDetail.userEstimatedTime!!
                        )
                    binding.tvCourseLikeCnt.text = it.courseDetail.likes.toString()
                    if(it.courseDetail.liked) {
                        binding.ivCourseLike.setImageResource(R.drawable.ic_course_like)
                    } else {
                        binding.ivCourseLike.setImageResource(R.drawable.ic_course_like_inactive)
                    }

                    withContext(Dispatchers.IO) {
                        getGpxInputStream(it.courseDetail.gpxFile)?.let { inputStream ->
                            drawGpxFile(inputStream)
                        }
                    }
                }
            }
        }

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
            }

            override fun getPosition(): LatLng {
                val fallbackLocation = LocationUtils.getFallbackLocation()
                return LatLng.from(fallbackLocation.latitude, fallbackLocation.longitude)
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

    private fun drawGpxFile(inputStream: InputStream) {
        CoroutineScope(Dispatchers.IO).launch {
            mContext?.let {
                val trackPoints = parseGpx(inputStream)
                withContext(Dispatchers.Main) {
                    val startPoint = trackPoints[0]
                    val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                        LatLng.from(
                            startPoint.lat,
                            startPoint.lon
                        )
                    )
                    kakaoMap?.let { map ->
                        map.moveCamera(cameraUpdate)
                        addCourseLine(it, map, trackPoints)
                    }
                }
            }
        }
    }
}