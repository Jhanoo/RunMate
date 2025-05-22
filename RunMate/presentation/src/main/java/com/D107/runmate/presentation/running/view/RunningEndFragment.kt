package com.D107.runmate.presentation.running.view

import android.content.Context
import android.os.Bundle
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
import com.D107.runmate.presentation.course.view.CourseAddDialog
import com.D107.runmate.presentation.databinding.FragmentRunningEndBinding
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
import com.D107.runmate.presentation.running.Coord2AddressState
import com.D107.runmate.presentation.running.RunningEndState
import com.D107.runmate.presentation.running.CourseViewModel
import com.D107.runmate.presentation.running.HistoryDetailState
import com.D107.runmate.presentation.running.RunningEndViewModel
import com.D107.runmate.presentation.utils.CommonUtils.convertDateTime
import com.D107.runmate.presentation.utils.CommonUtils.dateformatMMdd
import com.D107.runmate.presentation.utils.CommonUtils.getGpxInputStream
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
import com.D107.runmate.presentation.utils.LocationUtils.getPaceFromSpeed
import com.D107.runmate.presentation.utils.SourceScreen
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
    private val courseViewModel: CourseViewModel by viewModels()
    private lateinit var dialog: CourseAddDialog
    private var mContext: Context? = null
    private var isLike = false
    private var initIsLike = false

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

        mainViewModel.historyId.value?.let {
            courseViewModel.getHistoryDetail(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.runningRecord.collectLatest {
                if (it is RunningRecordState.Exist) {
                    Timber.d("running record $it")
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
                    val startLocation = mainViewModel.userLocation.value
                    binding.tvDistance.text = getString(R.string.course_distance, lastRecord.distance)
                    binding.tvDateGroupInfo.text = getString(R.string.running_date, firstRecord.currentTime, lastRecord.currentTime)
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
                    binding.tvCalorie.text = "0"

                    if(startLocation is UserLocationState.Exist) {
                        courseViewModel.getAddressFromLatLng(startLocation.locations.last().longitude, startLocation.locations.last().latitude)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.historyDetail.collectLatest { state ->
                when (state) {
                    is HistoryDetailState.Success -> {
                        isLike = state.historyDetail.myRunItem.courseLiked
                        initIsLike = state.historyDetail.myRunItem.courseLiked
                        if(state.historyDetail.myRunItem.courseLiked) {
                            binding.ivLike.setImageResource(R.drawable.ic_course_like)
                        } else {
                            binding.ivLike.setImageResource(R.drawable.ic_course_like_inactive)
                        }
                    }

                    is HistoryDetailState.Error -> {
                        Timber.d("getHistoryDetail Error {${state.message}}")
                    }

                    is HistoryDetailState.Initial -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.courseCreate.collectLatest {
                if(it) {
                    binding.btnAddCourse.visibility = View.GONE
                }
            }


        }
        if (sourceFragment == SourceScreen.GROUP_RUNNING_FRAGMENT) {
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
            mainViewModel.resetHistoryId()
            if(isLike != initIsLike) {
                mainViewModel.course.value.first?.let {
                    courseViewModel.updateCourseLike(it)
                }
            }
            when (sourceFragment) {
                SourceScreen.RUNNING_FRAGMENT -> findNavController().navigate(R.id.action_runningEndFragment_to_runningFragment)
                SourceScreen.GROUP_RUNNING_FRAGMENT -> groupViewModel.getCurrentGroup()
            }
        }

        binding.btnChart.setOnClickListener {
            val historyDetail = courseViewModel.historyDetail.value
            if(historyDetail is HistoryDetailState.Success) {
                val action = RunningEndFragmentDirections.actionRunningEndFragmentToChartFragment(historyDetail.historyDetail.gpxFile)
                findNavController().navigate(action)
            }
        }

        binding.btnAddCourse.setOnClickListener {
            dialog = CourseAddDialog() {
                val record = mainViewModel.runningRecord.value
                if(record is RunningRecordState.Exist) {
                    val lastRecord = record.runningRecords.last()

                    var name = "${mainViewModel.nickname} ${dateformatMMdd(record.runningRecords.first().currentTime)}"

                    if(it.first.isNotEmpty()) {
                        name = it.first
                    }
                    courseViewModel.createCourse(lastRecord.altitudeSum/mainViewModel.recordSize.value, lastRecord.distance,
                        mainViewModel.historyId.value!!, name, it.second, courseViewModel.address.value!!)
                }
            }
            dialog.show(requireActivity().supportFragmentManager, "course_add")
        }

        binding.ivLike.setOnClickListener {
            if(isLike == false) {
                binding.ivLike.setImageResource(R.drawable.ic_course_like)
                isLike = true
            } else {
                binding.ivLike.setImageResource(R.drawable.ic_course_like_inactive)
                isLike = false
            }
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
        if (mainViewModel.course.value.first == null) {
            // 프리 모드인 경우
            binding.btnAddCourse.visibility = View.VISIBLE
            binding.ivLike.visibility = View.GONE
        } else {
            // 코스 모드인 경우
            binding.btnAddCourse.visibility = View.GONE
            binding.ivLike.visibility = View.VISIBLE
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