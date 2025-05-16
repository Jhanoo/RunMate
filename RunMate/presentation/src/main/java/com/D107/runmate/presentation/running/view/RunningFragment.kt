package com.D107.runmate.presentation.running.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.RunningTrackingService
import com.D107.runmate.presentation.databinding.FragmentRunningBinding
import com.D107.runmate.presentation.running.RunningEndViewModel
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.CommonUtils.getActivityContext
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCoursePoint
import com.D107.runmate.presentation.utils.LocationUtils
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "RunningFragment"

@AndroidEntryPoint
class RunningFragment : BaseFragment<FragmentRunningBinding>(
    FragmentRunningBinding::bind,
    R.layout.fragment_running
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val runningEndViewModel: RunningEndViewModel by viewModels()
    private var mContext: Context? = null
    private var userLabel: Label? = null
    private lateinit var dialog: PaceSettingDialog
    private var setting: List<Int> = listOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.courseId.value?.let {
            Timber.d("courseId ${it}")
        }

        if (mainViewModel.userLocation.value is UserLocationState.Initial) {
            viewLifecycleOwner.lifecycleScope.launch {
                mContext?.let {
                    val location =
                        LocationUtils.getLocation(it, (getActivityContext(it) as MainActivity))
                    mainViewModel.setUserLocation(
                        UserLocationState.Exist(
                            listOf(
                                LocationModel(
                                    location.latitude,
                                    location.longitude,
                                    location.altitude,
                                    location.speed
                                )
                            )
                        )
                    )
                }
            }
        }

        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Log.d(TAG, "onMapError: ${p0?.message}")
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
                loadLocationAndMove()
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

        initEvent()

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.time.collectLatest { it ->
                binding.tvTime.text = getString(R.string.running_time, it / 60, it % 60)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.trackingStatus.collectLatest { status ->
                    when (mainViewModel.trackingStatus.value) {
                        TrackingStatus.STOPPED -> {
                            findNavController().navigate(R.id.action_runningFragment_to_runningEndFragment)
                        }

                        TrackingStatus.RUNNING -> {
                            binding.groupBtnStart.visibility = View.INVISIBLE
                            binding.groupRecord.visibility = View.VISIBLE
                            binding.groupBtnPause.visibility = View.GONE
                            binding.groupBtnRunning.visibility = View.VISIBLE
                            mContext?.let {
                                (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                            }
                        }

                        TrackingStatus.INITIAL -> {
                            binding.groupBtnStart.visibility = View.VISIBLE
                            binding.groupRecord.visibility = View.GONE
                            binding.groupBtnPause.visibility = View.GONE
                            binding.groupBtnRunning.visibility = View.GONE
                            mContext?.let {
                                (getActivityContext(it) as MainActivity).showHamburgerBtn()
                            }
                            runningEndViewModel.deleteFile()
                        }

                        TrackingStatus.PAUSED -> {
                            binding.groupBtnStart.visibility = View.INVISIBLE
                            binding.groupRecord.visibility = View.VISIBLE
                            binding.groupBtnPause.visibility = View.VISIBLE
                            binding.groupBtnRunning.visibility = View.GONE
                            mContext?.let {
                                (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.runningRecord.collectLatest { state ->
                if (state is RunningRecordState.Exist) {
                    if (state.runningRecords.size > 1) {
                        val locationValue = mainViewModel.userLocation.value
                        if (locationValue is UserLocationState.Exist) {
                            val currentLocation = LatLng.from(
                                locationValue.locations.last().latitude,
                                locationValue.locations.last().longitude
                            )
                            val prevLocation = LatLng.from(
                                locationValue.locations[locationValue.locations.size - 2].latitude,
                                locationValue.locations[locationValue.locations.size - 2].longitude
                            )
                            mContext?.let {
                                kakaoMap?.let {
                                    addCoursePoint(
                                        mContext!!,
                                        kakaoMap!!,
                                        prevLocation,
                                        currentLocation
                                    )
                                }
                            }

                        }
                    }
                    binding.tvDistance.text =
                        getString(R.string.running_distance, state.runningRecords.last().distance)
                    binding.tvPace.text =
                        LocationUtils.getPaceFromSpeed(state.runningRecords.last().currentSpeed)
                } else {
                    Log.d(TAG, "onViewCreated: state else")
                }
            }
        }
    }

    private fun initEvent() {
        binding.bgRunningRecord.setOnClickListener {
            findNavController().navigate(R.id.action_runningFragment_to_runningRecordFragment)
        }

        binding.btnSetCourse.setOnClickListener {
            findNavController().navigate(R.id.action_runningFragment_to_courseSettingFragment)
        }

        binding.btnStart.setOnClickListener {
            mContext?.let {
                RunningTrackingService.startService(it)
                (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                binding.groupBtnRunning.visibility = View.VISIBLE
                binding.groupBtnStart.visibility = View.INVISIBLE
                binding.groupRecord.visibility = View.VISIBLE
            }
        }

        binding.btnPause.setOnClickListener {
            binding.groupBtnPause.visibility = View.VISIBLE
            binding.groupBtnRunning.visibility = View.GONE
            mContext?.let {
                RunningTrackingService.pauseService(it)
            }
        }

        binding.btnRestart.setOnClickListener {
            binding.groupBtnPause.visibility = View.GONE
            binding.groupBtnRunning.visibility = View.VISIBLE
            mContext?.let {
                RunningTrackingService.startService(it)
            }
        }

        binding.btnEnd.setOnClickListener {
            mContext?.let {
                RunningTrackingService.stopService(it)
//                findNavController().navigate(R.id.action_runningFragment_to_runningEndFragment)
            }
        }

        binding.btnVibrate.setOnClickListener {
            if (mainViewModel.isVibrationEnabled.value) {
                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_off)
            } else {
                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_on)
            }
//            mainViewModel.toggleVibrationEnabled()
        }

        binding.btnSound.setOnClickListener {
            if (mainViewModel.isSoundEnabled.value) {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_off)
            } else {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_on)
            }
//            mainViewModel.toggleSoundEnabled()
        }

        binding.btnSetPace.setOnClickListener {
            dialog = PaceSettingDialog(setting) { value ->
                if (value.size != 0) {
                    mainViewModel.setGoalPace(value[0] * 60 + value[1])
                    Log.d(TAG, "onViewCreated: value ${value[0]} ${value[1]}")
                } else {
                    mainViewModel.setGoalPace(null)
                    Log.d(TAG, "onViewCreated: value is empty")
                }
                setting = value
            }
            dialog.show(requireActivity().supportFragmentManager, "pace")
        }
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        mContext?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val profileUrl = "https://picsum.photos/200/300" // 테스트 이미지, 추후 사용자 프로필 이미지로 변경 예정
                val profileBitmap = getBitmapFromURL(profileUrl)
                profileBitmap?.let { bitmap ->
                    val markerBg =
                        BitmapFactory.decodeResource(
                            it.resources,
                            R.drawable.bg_running_marker_blue
                        )
                    val resizedMarkerBg =
                        Bitmap.createScaledBitmap(
                            markerBg,
                            CommonUtils.dpToPx(it, 40f),
                            CommonUtils.dpToPx(it, 60f),
                            true
                        )

                    val combinedBitmap = createProfileMarker(resizedMarkerBg, bitmap)
                    val profilePng = bitmapToPng(combinedBitmap)

                    withContext(Dispatchers.Main) {
                        kakaoMap?.let { map ->
                            userLabel?.remove()

                            val styles = map.labelManager
                                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(profilePng)))
                            val options = LabelOptions.from(LatLng.from(latitude, longitude))
                                .setStyles(styles)
                            val layer = map.labelManager!!.layer
                            userLabel = layer!!.addLabel(options)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()

        when (mainViewModel.trackingStatus.value) {
            TrackingStatus.STOPPED -> {
                // 종료
            }

            TrackingStatus.RUNNING -> {
                binding.groupBtnStart.visibility = View.INVISIBLE
                binding.groupRecord.visibility = View.VISIBLE
                binding.groupBtnPause.visibility = View.GONE
                binding.groupBtnRunning.visibility = View.VISIBLE
                mContext?.let {
                    (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                }
            }

            TrackingStatus.INITIAL -> {
                binding.groupBtnStart.visibility = View.VISIBLE
                binding.groupRecord.visibility = View.GONE
                binding.groupBtnPause.visibility = View.GONE
                binding.groupBtnRunning.visibility = View.GONE
                mContext?.let {
                    (getActivityContext(it) as MainActivity).showHamburgerBtn()
                }
                runningEndViewModel.deleteFile()
            }

            TrackingStatus.PAUSED -> {
                binding.groupBtnStart.visibility = View.INVISIBLE
                binding.groupRecord.visibility = View.VISIBLE
                binding.groupBtnPause.visibility = View.VISIBLE
                binding.groupBtnRunning.visibility = View.GONE
                mContext?.let {
                    (getActivityContext(it) as MainActivity).hideHamburgerBtn()
                }
            }
        }
    }

    private fun loadLocationAndMove() {
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.userLocation.collectLatest { state ->
                when (state) {
                    is UserLocationState.Exist -> {
                        Timber.d("loadLocationAndMove UserLocationState Exist")
                        val tmpUserLabel = userLabel
                        if (tmpUserLabel != null) {
                            tmpUserLabel.moveTo(
                                LatLng.from(
                                    state.locations.last().latitude,
                                    state.locations.last().longitude
                                ), 800)
                        } else {
                            val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                                LatLng.from(
                                    state.locations.last().latitude,
                                    state.locations.last().longitude
                                )
                            )
                            kakaoMap?.moveCamera(cameraUpdate)
                            addMarker(
                                state.locations.last().latitude,
                                state.locations.last().longitude
                            )
                        }
                    }

                    is UserLocationState.Initial -> {
                        Timber.d("loadLocationAndMove UserLocationState Initial")
                    }

                    else -> {
                        Timber.d("loadLocationAndMove UserLocationState else")
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
        userLabel = null
    }

    private fun createProfileMarker(markerBackground: Bitmap, profileBitmap: Bitmap): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            markerBackground.width,
            markerBackground.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)

        canvas.drawBitmap(markerBackground, 0f, 0f, null)

        val profileSize = (markerBackground.width * 0.76).toInt()
        val profileTop = (markerBackground.height * 0.08).toFloat()
        val profileLeft = (markerBackground.width - profileSize) / 2f

        val scaledProfile = Bitmap.createScaledBitmap(profileBitmap, profileSize, profileSize, true)

        val output = Bitmap.createBitmap(profileSize, profileSize, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas2.drawCircle(profileSize / 2f, profileSize / 2f, profileSize / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas2.drawBitmap(scaledProfile, 0f, 0f, paint)

        canvas.drawBitmap(output, profileLeft, profileTop, null)

        return resultBitmap
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        try {
            val url = URL(src)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                doInput = true
                connect()
            }
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun bitmapToPng(bitmap: Bitmap): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}