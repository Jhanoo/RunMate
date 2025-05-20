package com.D107.runmate.presentation.history.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.course.view.CourseAddDialog
import com.D107.runmate.presentation.databinding.FragmentPersonalHistoryBinding
import com.D107.runmate.presentation.history.HistoryViewModel
import com.D107.runmate.presentation.history.UserHistoryDetailState
import com.D107.runmate.presentation.running.CourseDetailState
import com.D107.runmate.presentation.running.CourseViewModel
import com.D107.runmate.presentation.running.HistoryDetailState
import com.D107.runmate.presentation.running.RunningEndViewModel
import com.D107.runmate.presentation.running.view.RunningEndFragmentDirections
import com.D107.runmate.presentation.utils.CommonUtils.dateformatMMdd
import com.D107.runmate.presentation.utils.CommonUtils.getGpxInputStream
import com.D107.runmate.presentation.utils.CommonUtils.getSecondsBetween
import com.D107.runmate.presentation.utils.GpxParser
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
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
import java.io.InputStream

@AndroidEntryPoint
class PersonalHistoryFragment : BaseFragment<FragmentPersonalHistoryBinding>(
    FragmentPersonalHistoryBinding::bind,
    R.layout.fragment_personal_history
) {
    private var mContext: Context? = null
    private var isLike = false
    private var initIsLike = false
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private lateinit var dialog: CourseAddDialog
    private val args: PersonalHistoryFragmentArgs by navArgs()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initEvent()
        initMap()

//        historyViewModel.getGroupUserHistoryDetail(args.groupId, args.userId)

        viewLifecycleOwner.lifecycleScope.launch {
            historyViewModel.historyUserDetail.collectLatest { state ->
                when (state) {
                    is UserHistoryDetailState.Success -> {
                        Timber.d("state Success {${state.userHistoryDetail}}")
                        val time = getSecondsBetween(state.userHistoryDetail.startTime, state.userHistoryDetail.endTime)
                        binding.tvDistance.text = getString(R.string.course_distance, state.userHistoryDetail.distance)
                        binding.tvDateGroupInfo.text = getString(R.string.running_date, state.userHistoryDetail.startTime, state.userHistoryDetail.endTime)
                        binding.tvTime.text = getString(R.string.running_time, time / 60, time % 60)
                        binding.tvBpm.text = "-" // TODO 추후 HR 연결하여 데이터 수정
                        binding.tvAvgPace.text = getString(R.string.running_pace, (state.userHistoryDetail.avgPace.toInt())/60, (state.userHistoryDetail.avgPace.toInt())%60)
                        binding.tvCadence.text = getString(R.string.running_avg_cadence, state.userHistoryDetail.avgCadence)
                        binding.tvAltitude.text = getString(R.string.running_avg_altitude, state.userHistoryDetail.avgElevation)
                        binding.tvCalorie.text = state.userHistoryDetail.calories.toString()
                    }

                    is UserHistoryDetailState.Error -> {
                        Timber.d("getHistoryDetail Error {${state.message}}")
                    }

                    is UserHistoryDetailState.Initial -> {
                        Timber.d("getHistoryDetail Initial")
                    }
                }
            }
        }
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

    private fun initEvent() {
        binding.btnNext.setOnClickListener {
            if(isLike != initIsLike) {
                mainViewModel.course.value.first?.let {
                    courseViewModel.updateCourseLike(it)
                }
            }
            findNavController().popBackStack()
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
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        if(historyViewModel.historyUserDetail.value is UserHistoryDetailState.Success){
                            (historyViewModel.historyUserDetail.value as UserHistoryDetailState.Success).userHistoryDetail.gpxFile?.let{
                                withContext(Dispatchers.IO) {
                                    drawGpxFile(GpxParser.getGpxInputStream(it))
                                }
                            }
                        }
                    }
                }
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
//        historyViewModel.resetHistoryDetail()
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
                        Timber.d("addCourseLine")
                        map.moveCamera(cameraUpdate)
                        addCourseLine(it, map, trackPoints)
                    }
                }
            }
        }
    }
}