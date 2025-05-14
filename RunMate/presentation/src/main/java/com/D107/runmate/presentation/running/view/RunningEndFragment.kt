package com.D107.runmate.presentation.running.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentRunningEndBinding
import com.D107.runmate.presentation.running.Coord2AddressState
import com.D107.runmate.presentation.running.RunningEndState
import com.D107.runmate.presentation.running.RunningEndViewModel
import com.D107.runmate.presentation.utils.CommonUtils.convertDateTime
import com.D107.runmate.presentation.utils.CommonUtils.getGpxInputStream
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
import com.D107.runmate.presentation.utils.LocationUtils
import com.D107.runmate.presentation.utils.LocationUtils.getPaceFromSpeed
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

@AndroidEntryPoint
class RunningEndFragment : BaseFragment<FragmentRunningEndBinding>(
    FragmentRunningEndBinding::bind,
    R.layout.fragment_running_end
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val runningEndViewModel: RunningEndViewModel by viewModels()
    private lateinit var dialog: CourseAddDialog
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initEvent()
        initMap()

        viewLifecycleOwner.lifecycleScope.launch {
            runningEndViewModel.endRunning.collectLatest {
                when (it) {
                    is RunningEndState.Error -> {
                        Timber.d("runningend error ${it.message}")
                        mainViewModel.setTrackingStatus(TrackingStatus.INITIAL)
                        findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
                    }
                    is RunningEndState.Success -> {
                        mainViewModel.setTrackingStatus(TrackingStatus.INITIAL)

                        findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            runningEndViewModel.coord2Address.collectLatest {
                when (it) {
                    is Coord2AddressState.Error -> {
                        Timber.d("coord2address error ${it.message}")
                        mainViewModel.setTrackingStatus(TrackingStatus.INITIAL)
                        findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
                    }
                    is Coord2AddressState.Success -> {
                        val record = mainViewModel.runningRecord.value
                        val locations = mainViewModel.userLocation.value
                        if(record is RunningRecordState.Exist && locations is UserLocationState.Exist) {
                            runningEndViewModel.endRunning(
                                0.0,
                                record.runningRecords.last().cadenceSum/record.runningRecords.size,
                                record.runningRecords.last().altitudeSum/record.runningRecords.size,
                                16.6667 / record.runningRecords.last().avgSpeed,
                                0.0,
                                mainViewModel.courseId.value,
                                (record.runningRecords.last().distance).toDouble(),
                                convertDateTime(record.runningRecords.last().currentTime),
                                it.address.address_name,
                                convertDateTime(record.runningRecords.first().currentTime)
                            )
                        } else {
                            mainViewModel.setTrackingStatus(TrackingStatus.INITIAL)
                            findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
                        }
                    }
                }
            }
        }

    }

    private fun initEvent() {
        binding.btnNext.setOnClickListener {
            val location = mainViewModel.userLocation.value
            if(location is UserLocationState.Exist) {
                runningEndViewModel.getCoord2Address(location.locations.first().longitude, location.locations.first().latitude)
            }
        }

        binding.btnChart.setOnClickListener {
            findNavController().navigate(R.id.action_runningEndFragment_to_chartFragment)
        }

        binding.btnAddCourse.setOnClickListener {
            dialog = CourseAddDialog()
            dialog.show(requireActivity().supportFragmentManager, "course_add")
        }
        
        binding.ivLike.setOnClickListener { 
            // TODO 사용자가 이미 좋아요한 경우
            binding.ivLike.setImageResource(R.drawable.ic_course_like_inactive)

            // TODO 사용자가 처음 좋아요하는 경우
            binding.ivLike.setImageResource(R.drawable.ic_course_like)

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
//            override fun getPosition(): LatLng {
//                // TODO 코스의 시작지점을 LatLng으로 반환
//                mainViewModel.userLocation.value?.let {
//                    if (it is UserLocationState.Exist) {
//                        return LatLng.from(
//                            it.locations.last().latitude,
//                            it.locations.last().longitude
//                        )
//                    }
//                }
//                val fallbackLocation = LocationUtils.getFallbackLocation()
//                return LatLng.from(fallbackLocation.latitude, fallbackLocation.longitude)
//            }
        })
    }

    private fun initUI() {
        if (mainViewModel.courseId.value == null) {
            // 프리 모드인 경우
            binding.btnAddCourse.visibility = View.VISIBLE
            binding.ivLike.visibility = View.GONE
        } else {
            // 코스 모드인 경우
            binding.btnAddCourse.visibility = View.GONE
            binding.ivLike.visibility = View.VISIBLE

            // TODO 사용자 좋아요 여부 좋아요 x
            binding.ivLike.setImageResource(R.drawable.ic_course_like_inactive)

            // TODO 사용자 좋아요 여부 좋아요 o
            binding.ivLike.setImageResource(R.drawable.ic_course_like)
        }

        CoroutineScope(Dispatchers.IO).launch {
            mContext?.let {
                getGpxInputStream(it)?.let { inputStream ->
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

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }

}