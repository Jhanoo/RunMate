package com.D107.runmate.presentation.group.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.RunningRecordState
import com.D107.runmate.domain.model.running.TrackingStatus
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.domain.model.socket.MemberLocationData
import com.D107.runmate.domain.model.socket.SocketAuth
import com.D107.runmate.presentation.MainActivity
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.RunningTrackingService
import com.D107.runmate.presentation.databinding.FragmentGroupRunningBinding
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
import com.D107.runmate.presentation.group.viewmodel.SocketAuthParcelable
import com.D107.runmate.presentation.running.RunningEndViewModel
import com.D107.runmate.presentation.running.view.PaceSettingDialog
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.CommonUtils.getActivityContext
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
import kotlinx.coroutines.flow.first
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
    private val runningEndViewModel: RunningEndViewModel by viewModels()
    private val viewModel: GroupViewModel by activityViewModels()
    private var mContext: Context? = null
    private lateinit var dialog: PaceSettingDialog
    private var setting: List<Int> = listOf()
    var userLabels: HashMap<String, Label> = hashMapOf()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListenner()
        initmap()
        observeViewModel()
        viewModel.hasGroupHistory()
        viewModel.connectToServer(
            SocketAuth(
                mainViewModel.userId.value!!,
                mainViewModel.nickname.value!!,
                mainViewModel.profileImage.value
            )
        )
    }


    private fun setClickListenner() {

        binding.bgRunningRecord.setOnClickListener {
            findNavController().navigate(R.id.action_groupRunningFragment_to_runningRecordFragment)
        }

        binding.btnLeaveGroup.setOnClickListener {
            viewModel.leaveGroupSocket()
            viewModel.leaveGroup()
        }

        binding.btnStart.setOnClickListener {
            mContext?.let {
                RunningTrackingService.startService(
                    context = it,
                    groupId = viewModel.currentGroup.value?.groupId,
                    groupLeaderId = viewModel.currentGroup.value?.leaderId,
                    socketAuth = SocketAuthParcelable(
                        userId = mainViewModel.userId.value!!,
                        nickname = mainViewModel.nickname.value!!,
                        profileImage = mainViewModel.profileImage.value
                    )
                )
                (getActivityContext(requireContext()) as MainActivity).hideHamburgerBtn()
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
                RunningTrackingService.pauseService(it, viewModel.currentGroup.value?.groupId)
            }
        }

        binding.btnRestart.setOnClickListener {
            binding.groupBtnPause.visibility = View.GONE
            binding.groupBtnRunning.visibility = View.VISIBLE
            mContext?.let {
                RunningTrackingService.startService(
                    context = it,
                    groupId = viewModel.currentGroup.value?.groupId,
                    groupLeaderId = viewModel.currentGroup.value?.leaderId,
                    socketAuth = SocketAuthParcelable(
                        userId = mainViewModel.userId.value!!,
                        nickname = mainViewModel.nickname.value!!,
                        profileImage = mainViewModel.profileImage.value
                    )
                )
            }
        }

        binding.btnEnd.setOnClickListener {
            mContext?.let {
                Timber.d("Stop Group Running!")
//                viewModel.disConnectSocket()
                RunningTrackingService.stopService(it, viewModel.currentGroup.value?.groupId)
            }
        }

        binding.btnVibrate.setOnClickListener {
            viewModel.sendLocation(36.104342, 128.418323)
            if (mainViewModel.isVibrationEnabled.value) {

                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_off)
            } else {

                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_on)
            }
        }

        binding.btnSound.setOnClickListener {
            viewModel.sendLocation(36.108355, 128.415780)
            if (mainViewModel.isSoundEnabled.value) {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_off)
            } else {
                binding.btnSound.setImageResource(R.drawable.ic_running_btn_sound_on)
            }
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


        binding.btnFinishGroup.setOnClickListener {
            viewModel.leaveGroupSocket()
            viewModel.finishGroup()
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
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
//                launch {
//                    mainViewModel.userLocation.collect { state ->
//                        when (state) {
//                            is UserLocationState.Exist -> {
//                                val tmpUserLabel = userLabels[mainViewModel.userId.value!!]
//                                if (tmpUserLabel != null) {
//                                    tmpUserLabel.moveTo(
//                                        LatLng.from(
//                                            state.locations.last().latitude,
//                                            state.locations.last().longitude
//                                        ), 500
//                                    )
////                            kakaoMap?.trackingManager?.startTracking(userLabel)
//
//                                } else {
//                                    addMarker(
//                                        state.locations.last().latitude,
//                                        state.locations.last().longitude,
//                                        mainViewModel.userId.value!!,
//                                        mainViewModel.profileImage.value?:""
//                                    )
//                                }
//                                val cameraUpdate = CameraUpdateFactory.newCenterPosition(
//                                    LatLng.from(
//                                        state.locations.last().latitude,
//                                        state.locations.last().longitude
//                                    )
//                                )
//                                kakaoMap?.moveCamera(cameraUpdate)
//                            }
//
//                            is UserLocationState.Initial -> {
//                                Timber.d("onViewCreated: initial")
//                            }
//                        }
//                    }
//                }

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
                                findNavController().navigate(
                                    R.id.groupFragment,
                                    null,
                                    navigateOptions
                                )

                            }

                            is GroupUiEvent.ShowToast -> {
                                showToast(event.message)
                            }

                            is GroupUiEvent.GoToGroupInfo -> {

                            }
                            is GroupUiEvent.ToggleGroupRunningFinishVisible ->{
                                if(event.visible){
                                    binding.btnFinishGroup.visibility = View.VISIBLE
                                    binding.btnStart.visibility = View.GONE
                                    binding.btnSetPace.visibility = View.GONE
                                    binding.btnLeaveGroup.visibility = View.GONE

                                }else{
                                    binding.btnFinishGroup.visibility = View.GONE
                                    binding.btnStart.visibility = View.VISIBLE
                                    binding.btnSetPace.visibility = View.VISIBLE
                                    binding.btnLeaveGroup.visibility = View.VISIBLE
                                }
                            }

                            else -> {}

                        }
                    }
                }

                launch {
                    viewModel.groupMemberLocation.collect { groupMemberLocation ->
                        groupMemberLocation?.let {
                            Timber.d("onViewCreated: groupMemberLocation $it")
                            handleLocationUpdate(it)
                        }

                    }
                }

                mainViewModel.trackingStatus.collectLatest { status ->
                    when (mainViewModel.trackingStatus.value) {
                        TrackingStatus.STOPPED -> {
//                            viewModel.disConnectSocket()
                            val action =
                                GroupRunningFragmentDirections.actionGroupRunningFragmentToRunningEndFragment(
                                    sourceScreen = "GROUP_RUNNING_FRAGMENT"
                                )
                            findNavController().navigate(action)
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
                loadLocationAndMove()
                restoreOtherMemberMarkers()
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

    private fun addMarker(
        latitude: Double,
        longitude: Double,
        userId: String,
        userProfileUrl: String = "https://picsum.photos/200/300"
    ) {
        mContext?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val profileBitmap: Bitmap? = if (userProfileUrl.isNullOrEmpty()) {
                    BitmapFactory.decodeResource(resources, R.drawable.tonie)
                } else {
                    getBitmapFromURL(userProfileUrl)
                }
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
                            userLabels[userId]?.let { userLabel ->
                                userLabel.remove()
                            }

                            val styles = map.labelManager
                                ?.addLabelStyles(LabelStyles.from(LabelStyle.from(profilePng)))
                            val options = LabelOptions.from(LatLng.from(latitude, longitude))
                                .setStyles(styles)
                            val layer = map.labelManager!!.layer
                            userLabels[userId] = layer!!.addLabel(options)
                        }
                    }
                }
            }
        }
    }

    private fun restoreOtherMemberMarkers() {
        if (kakaoMap == null) return
        // ViewModel의 memberLastLocations (이것은 Repository를 통해 업데이트됨)
        lifecycleScope.launch {
            viewModel.groupMemberDatas.first().forEach { (userId, memberLocationData) ->

                addMarker(
                    memberLocationData.lat,
                    memberLocationData.lng,
                    userId,
                    memberLocationData.profileImage ?: ""
                )

            }
        }
    }

    private fun loadLocationAndMove() {
        Timber.d("map ready and move")
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.userLocation.collectLatest { state ->
                when (state) {
                    is UserLocationState.Exist -> {
                        Timber.d("UserLocationState Exist")
                        val tmpUserLabel = userLabels[mainViewModel.userId.value]
                        if (tmpUserLabel != null && viewModel.isUserLabel) {
                            tmpUserLabel.moveTo(
                                LatLng.from(
                                    state.locations.last().latitude,
                                    state.locations.last().longitude
                                ), 800
                            )
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
                                state.locations.last().longitude,
                                mainViewModel.userId.value!!,
                                mainViewModel.profileImage.value ?: ""
                            )
                            viewModel.isUserLabel = true
                        }
                    }

                    is UserLocationState.Initial -> {
                        Timber.d("UserLocationState Initial ")
                    }

                    else -> {
                        Timber.d("UserLocationState else ")
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

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
        viewModel.stopObservingLocationUpdates()
        viewModel.isUserLabel = false
//        userLabels[mainViewModel.userId.value]?.remove()

    }

    override fun onDestroyView() {
        super.onDestroyView()
//        userLabels[mainViewModel.userId.value]?.remove()
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
            Timber.d("image src$src")
            val url = URL(src)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                doInput = true
                connect()
            }
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            Timber.d("bitmap pharse error")
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

    fun handleLocationUpdate(locationData: MemberLocationData) {
        if (kakaoMap == null) {
            Timber.w("KakaoMap is not ready yet.")
            return
        }

        if (locationData.userId == mainViewModel.userId.value) {
            return
        }

        val mapPoint = LatLng.from(locationData.lat, locationData.lng)

        if (userLabels.containsKey(locationData.userId)) {
            val label = userLabels[locationData.userId]
            label?.moveTo(mapPoint, 500)
            Timber.d("Moved label for user ${locationData.userId} to ${locationData.lat}, ${locationData.lng}")
        } else {
            // 새로운 유저: Label 생성 및 추가
            val labelOptions = LabelOptions.from(mapPoint)
                .setTexts(LabelTextBuilder().setTexts(locationData.nickname)) // 닉네임으로 Label 텍스트 설정

            addMarker(
                locationData.lat,
                locationData.lng,
                locationData.userId,
                locationData.profileImage ?: ""
            )
        }
    }


}

