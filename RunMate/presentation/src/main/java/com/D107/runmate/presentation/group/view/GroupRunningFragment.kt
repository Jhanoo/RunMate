package com.D107.runmate.presentation.group.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.RunningTrackingService
import com.D107.runmate.presentation.databinding.FragmentGroupRunningBinding
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
import com.D107.runmate.presentation.running.view.PaceSettingDialog
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.LocationUtils
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.ssafy.locket.presentation.base.BaseFragment
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


class GroupRunningFragment : BaseFragment<FragmentGroupRunningBinding>(
    FragmentGroupRunningBinding::bind,
    R.layout.fragment_group_running
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: GroupViewModel by activityViewModels()
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

        setClickListenner()
        initmap()
        observeViewModel()
        viewModel.connectToServer()
    }


    private fun setClickListenner() {

        binding.bgRunningRecord.setOnClickListener {
            findNavController().navigate(R.id.action_runningFragment_to_runningRecordFragment)
        }

        binding.btnLeaveGroup.setOnClickListener {
            findNavController().navigate(R.id.action_runningFragment_to_courseSettingFragment)
        }

        binding.btnStart.setOnClickListener {
            mContext?.let {
                RunningTrackingService.startService(it)
                (CommonUtils.getActivityContext(requireContext()) as MainActivity).hideHamburgerBtn()
                viewModel.joinGroupSocket()
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
                viewModel.disConnectSocket()
                RunningTrackingService.stopService(it)
//                findNavController().navigate(R.id.action_runningFragment_to_runningEndFragment)
            }
        }

        binding.btnVibrate.setOnClickListener {
            viewModel.sendLocation(36.104342, 128.418323)
            if (mainViewModel.isVibrationEnabled.value) {

                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_off)
            } else {

                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_on)
            }
//            mainViewModel.toggleVibrationEnabled()
        }

        binding.btnSound.setOnClickListener {
            viewModel.sendLocation(36.108355, 128.415780)
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
                    Timber.d("onViewCreated: value ${value[0]} ${value[1]}")
                } else {
                    Timber.d("onViewCreated: value is empty")
                }
                setting = value
            }
            dialog.show(requireActivity().supportFragmentManager, "pace")
        }

        binding.btnLeaveGroup.setOnClickListener {
            viewModel.leaveGroup()
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.userLocation.collect { state ->
                        when (state) {
                            is UserLocationState.Exist -> {
                                val tmpUserLabel = userLabel
                                if (tmpUserLabel != null) {
                                    tmpUserLabel.moveTo(
                                        LatLng.from(
                                            state.locations.last().latitude,
                                            state.locations.last().longitude
                                        ), 500
                                    )
//                            kakaoMap?.trackingManager?.startTracking(userLabel)

                                } else {
                                    addMarker(
                                        state.locations.last().latitude,
                                        state.locations.last().longitude,
                                        ""
                                    )
                                }
                                val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                                    LatLng.from(
                                        state.locations.last().latitude,
                                        state.locations.last().longitude
                                    )
                                )
                                kakaoMap?.moveCamera(cameraUpdate)
                            }

                            is UserLocationState.Initial -> {
                                Timber.d("onViewCreated: initial")
                            }
                        }
                    }
                }

                launch {
                    mainViewModel.time.collectLatest { it ->
                        binding.tvTime.text = getString(R.string.running_time, it / 60, it % 60)
                    }
                }

                launch {
                    mainViewModel.runningRecord.collectLatest { state ->
                        if (state is RunningRecordState.Exist) {
                            Timber.d("onViewCreated: state ${state.runningRecords.last().distance}")
                            binding.tvDistance.text = getString(
                                R.string.running_distance,
                                state.runningRecords.last().distance
                            )
                            binding.tvPace.text =
                                LocationUtils.getPaceFromSpeed(state.runningRecords.last().currentSpeed)
                        } else {
                            Timber.d("onViewCreated: state else")
                        }
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is GroupUiEvent.GoToGroup -> {
                                val navigateOptions = NavOptions.Builder()
                                    .setLaunchSingleTop(true)
                                    .setPopUpTo(R.id.groupRunningFragment, true)
                                    .build()
                                findNavController().navigate(R.id.groupFragment,null,navigateOptions)

                            }
                            is GroupUiEvent.ShowToast -> {
                                showToast(event.message)
                            }
                            else -> {}

                        }
                    }
                }

                launch{
                    viewModel.groupMemberLocation.collect{ groupMemberLocation->
                        groupMemberLocation?.let{
                            Timber.d("onViewCreated: groupMemberLocation $it")
                            handleLocationUpdate(it)
                        }

                    }
                }
            }

        }

    }

    private fun initmap() {
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Timber.d("onMapError: ${p0?.message}")
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

    private fun addMarker(latitude: Double, longitude: Double, userId:String, userProfileUrl:String = "https://picsum.photos/200/300"){
        mContext?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val profileBitmap = getBitmapFromURL(userProfileUrl)
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
                        val styles = kakaoMap!!.labelManager
                            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(profilePng)))
                        val options = LabelOptions.from(LatLng.from(latitude, longitude))
                            .setStyles(styles).setTexts(LabelTextBuilder().setTexts(userId))
                        val layer = kakaoMap!!.labelManager!!.layer
                        viewModel.userLabels[userId] = layer!!.addLabel(options)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()

        (activity as? MainActivity)?.showHamburgerBtn()
        viewModel.startObservingLocationUpdates()

        when (mainViewModel.trackingStatus.value) {
            TrackingStatus.STOPPED -> {
                // 종료
            }

            TrackingStatus.RUNNING -> {
                binding.groupBtnStart.visibility = View.INVISIBLE
                binding.groupRecord.visibility = View.VISIBLE
                binding.groupBtnPause.visibility = View.GONE
                binding.groupBtnRunning.visibility = View.VISIBLE
            }

            TrackingStatus.INITIAL -> {
                binding.groupBtnStart.visibility = View.VISIBLE
                binding.groupRecord.visibility = View.GONE
                binding.groupBtnPause.visibility = View.GONE
                binding.groupBtnRunning.visibility = View.GONE
            }

            TrackingStatus.PAUSED -> {
                binding.groupBtnStart.visibility = View.INVISIBLE
                binding.groupRecord.visibility = View.VISIBLE
                binding.groupBtnPause.visibility = View.VISIBLE
                binding.groupBtnRunning.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
        viewModel.stopObservingLocationUpdates()
        userLabel?.remove()
        userLabel = null

    }

    override fun onDestroyView() {
        super.onDestroyView()
        userLabel?.remove()
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

    fun handleLocationUpdate(locationData: MemberLocationData) { // LocationData는 서버에서 받는 데이터 모델
        if (kakaoMap == null) {
            Timber.w("KakaoMap is not ready yet.")
            return
        }

        // 자기 자신의 위치 업데이트는 별도로 처리하거나 여기서 제외
        if (locationData.userId == viewModel.dummyAuth.userId) { // myUserId는 현재 클라이언트의 userId
            // 내 위치 마커 업데이트 로직 (필요하다면)
            return
        }

        val mapPoint = LatLng.from(locationData.lat, locationData.lng)

        if (viewModel.userLabels.containsKey(locationData.userId)) {
            // 기존 유저: Label 위치 업데이트
            val label = viewModel.userLabels[locationData.userId]
            label?.moveTo(mapPoint,500)
            Timber.d("Moved label for user ${locationData.userId} to ${locationData.lat}, ${locationData.lng}")
        } else {
            // 새로운 유저: Label 생성 및 추가
            val labelOptions = LabelOptions.from(mapPoint)
                .setTexts(LabelTextBuilder().setTexts(locationData.nickname)) // 닉네임으로 Label 텍스트 설정

            addMarker(locationData.lat,locationData.lng,locationData.userId)
//            kakaoMap?.labelManager?.layer?.addLabel(labelOptions)?.let { newLabel ->
//                viewModel.userLabels[locationData.userId] = newLabel
//                Timber.d("Added new label for user ${locationData.userId} at ${locationData.lat}, ${locationData.lng}")
//            }
        }
    }

}