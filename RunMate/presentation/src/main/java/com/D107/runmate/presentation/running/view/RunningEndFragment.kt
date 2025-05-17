package com.D107.runmate.presentation.running.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentRunningEndBinding
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class RunningEndFragment : BaseFragment<FragmentRunningEndBinding>(
    FragmentRunningEndBinding::bind,
    R.layout.fragment_running_end
) {
    private val args: RunningEndFragmentArgs by navArgs()
    lateinit var sourceFragment: String
    private val groupViewModel: GroupViewModel by activityViewModels()
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
        sourceFragment = args.sourceScreen
        initUI()
        initEvent()
        initMap()

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.runningRecord.collectLatest {
                if (it is RunningRecordState.Exist) {
                    val time = mainViewModel.time.value
                    val lastRecord = it.runningRecords.last()
                    val firstRecord = it.runningRecords.first()
                    binding.tvDistance.text =
                        getString(R.string.course_distance, lastRecord.distance)
                    binding.tvDateGroupInfo.text = getString(
                        R.string.running_date,
                        firstRecord.currentTime,
                        lastRecord.currentTime
                    )
                    binding.tvTime.text = getString(R.string.running_time, time / 60, time % 60)
                    binding.tvBpm.text = "-" // TODO 추후 HR 연결하여 데이터 수정
                    binding.tvAvgPace.text = getPaceFromSpeed(lastRecord.avgSpeed)
                    binding.tvCadence.text = getString(
                        R.string.running_avg_cadence,
                        lastRecord.cadenceSum / it.runningRecords.size
                    )
                    binding.tvAltitude.text = getString(
                        R.string.running_avg_altitude,
                        lastRecord.altitudeSum / it.runningRecords.size
                    )
                    binding.tvCalorie.text = "0" // TODO 삼성헬스 연결하여 데이터 수정
                }
            }


        }
        if (sourceFragment == "GROUP_RUNNING_FRAGMENT") {
            viewLifecycleOwner.lifecycleScope.launch {
                groupViewModel.uiEvent.collect { event ->
                    Timber.d("groupUiEvent $event")
                    when (event) {
                        is GroupUiEvent.GoToGroupRunning -> {
                            findNavController().navigate(R.id.action_runningEndFragment_to_groupRrunningFragment)
                        }

                        is GroupUiEvent.GoToGroup -> {
                            findNavController().navigate(R.id.action_runningEndFragment_to_groupFragment)
                        }

                        else -> {

                        }
                    }

                }

            }
        }
    }

    private fun initEvent() {
        binding.btnNext.setOnClickListener {
            runningEndViewModel.deleteFile()
            mainViewModel.setTrackingStatus(TrackingStatus.INITIAL)
            when (sourceFragment) {
                "RUNNING_FRAGMENT" -> findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
                "GROUP_RUNNING_FRAGMENT" -> groupViewModel.getCurrentGroup()
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
                loadAndDrawGpxFile()
            }
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
    }

    private fun loadAndDrawGpxFile() {
        CoroutineScope(Dispatchers.IO).launch {
            mContext?.let {
                getGpxInputStream(it)?.let { inputStream ->
                    val trackPoints = parseGpx(inputStream)
                    withContext(Dispatchers.Main) {
                        Timber.d("trackPoints size ${trackPoints.size}")
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