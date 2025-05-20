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
import android.util.Log
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
import com.D107.runmate.presentation.running.CourseDetailState
import com.D107.runmate.presentation.running.RunningEndViewModel
import com.D107.runmate.presentation.running.view.PaceSettingDialog

import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToHMS
import com.D107.runmate.presentation.utils.CommonUtils.getActivityContext
import com.D107.runmate.presentation.utils.GpxParser
import com.D107.runmate.presentation.utils.GpxParser.getGpxInputStream
import com.D107.runmate.presentation.utils.GpxParser.parseGpx
import com.D107.runmate.presentation.utils.KakaoMapUtil
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCourseLine
import com.D107.runmate.presentation.utils.KakaoMapUtil.addCoursePoint
import com.D107.runmate.presentation.utils.LocationUtils
import com.D107.runmate.presentation.utils.SourceScreen
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class GroupRunningFragment : BaseFragment<FragmentGroupRunningBinding>(
    FragmentGroupRunningBinding::bind,
    R.layout.fragment_group_running
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: GroupViewModel by activityViewModels()
    private val runningEndViewModel: RunningEndViewModel by viewModels()
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
//            viewModel.sendLocation(36.104342, 128.418323)
            if (mainViewModel.isVibration.value) {

                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_off)
            } else {

                binding.btnVibrate.setImageResource(R.drawable.ic_running_btn_vibrate_on)
            }
        }

        binding.btnSound.setOnClickListener {
//            viewModel.sendLocation(36.108355, 128.415780)
            if (mainViewModel.isSound.value) {
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
                    if (mainViewModel.userLocation.value is UserLocationState.Initial) {
                        mContext?.let {
                            val location =
                                LocationUtils.getLocation(
                                    it,
                                    (getActivityContext(it) as MainActivity)
                                )
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

                            is GroupUiEvent.ToggleGroupRunningFinishVisible -> {
                                if (event.visible) {
                                    binding.btnFinishGroup.visibility = View.VISIBLE
                                    binding.btnStart.visibility = View.GONE
                                    binding.btnSetPace.visibility = View.GONE
                                    binding.btnLeaveGroup.visibility = View.GONE

                                }
                            }

                            is GroupUiEvent.DrawCourse -> {
                                withContext(Dispatchers.IO) {
                                    if (viewModel.courseDetail.value is CourseDetailState.Success) {
                                        getGpxInputStream((viewModel.courseDetail.value as CourseDetailState.Success).courseDetail.gpxFile)?.let { inputStream ->
                                            drawGpxFile(inputStream)
                                        }
                                    }
                                }
                            }

                            else -> {}

                        }
                    }
                }



                launch {
                    mainViewModel.trackingStatus.collectLatest { status ->
                        when (mainViewModel.trackingStatus.value) {
                            TrackingStatus.STOPPED -> {
//                            viewModel.disConnectSocket()
                                val action =
                                    GroupRunningFragmentDirections.actionGroupRunningFragmentToRunningEndFragment(
                                        sourceScreen = SourceScreen.GROUP_RUNNING_FRAGMENT
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
                viewModel.currentGroup.value?.courseId?.let {
                    viewModel.getCourseDetail(it)
                }
            }

        }
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.groupMemberLocation.collect { groupMemberLocation ->
                    groupMemberLocation?.let {
                        Timber.d("onViewCreated: groupMemberLocation $it")
                        handleLocationUpdate(it)
                    }

                }
            }
            launch {
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
                            getString(
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
                val state = mainViewModel.userLocation.value
                val userId = mainViewModel.userId.value
                userId?.let {
                    userLabels.remove(userId)
                }
                if (state is UserLocationState.Exist) {
                    Timber.d("groupKakaoMap Ready: $state")
                    setCameraUpdateAndAddMarker(state)
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val latlngList:List<LatLng> = state.locations.map {
                                LatLng.from(it.latitude,it.longitude)
                            }
                            KakaoMapUtil.addMoveLine(requireContext(), kakaoMap!!, latlngList)
                        }

                    }

                }
                loadLocationAndMove()
                restoreOtherMemberMarkers()
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.courseDetail.value is CourseDetailState.Success) {
                        (viewModel.courseDetail.value as CourseDetailState.Success).courseDetail.gpxFile?.let {
                            withContext(Dispatchers.IO) {
                                drawGpxFile(GpxParser.getGpxInputStream(it))
                            }
                        }
                    }

                }
//                kakaoMap?.setOnLabelClickListener { kakaoMap, layer, label ->
//                    // 클릭된 Label 객체를 직접 가져올 수 있습니다.
//                    // label.tag 를 사용하거나, userLabels 맵에서 label 객체로 userId를 역으로 찾는 등의 방법으로
//                    // 어떤 Label이 클릭되었는지 식별합니다.
//
//                    val clickedUserId = findUserIdForLabel(label) // 아래 예시 함수
//                    val memberData = viewModel.currentGroup.value.members.let{
//                        [clickedUserId]
//                    }
//
//
//                    clickedUserId?.let { userId ->
//                        val userInfo = getUserInfoFromUserId(userId) // 사용자 정보 가져오기
//
//                        userInfo?.let { info ->
//                            // 여기에 클릭 시 동작 구현 (예: 다이얼로그 표시, 다른 화면으로 이동 등)
//                            // 만약 "길게 누르기" 대신 "클릭" 시 텍스트를 보여주고 싶다면,
//                            // 여기에 정보 레이블을 띄우는 로직을 넣을 수 있습니다.
//                            showInfoForUser(kakaoMap, label.position, info)
//                        }
//                    }
//                }

            }

            override fun getPosition(): LatLng {
                mainViewModel.userLocation.value.let {
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
                    val markerBg = if (userId == mainViewModel.userId.value) {
                        BitmapFactory.decodeResource(
                            it.resources,
                            R.drawable.bg_running_marker_blue
                        )
                    } else {
                        BitmapFactory.decodeResource(
                            it.resources,
                            R.drawable.bg_running_marker_green
                        )
                    }
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
                                .setClickable(true)
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
                            setCameraUpdateAndAddMarker(state)
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
        Timber.d("trackingStatus: ${mainViewModel.trackingStatus.value}")

        when (mainViewModel.trackingStatus.value) {
            TrackingStatus.STOPPED -> {
                // 종료
            }

            TrackingStatus.RUNNING -> {
                binding.groupBtnStart.visibility = View.INVISIBLE
                binding.groupRecord.visibility = View.VISIBLE
                binding.groupBtnPause.visibility = View.GONE
                binding.btnFinishGroup.visibility = View.GONE
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
                binding.btnFinishGroup.visibility = View.GONE
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

    private fun drawGpxFile(inputStream: InputStream) {
        CoroutineScope(Dispatchers.IO).launch {
            mContext?.let {
                val trackPoints = parseGpx(inputStream)
                withContext(Dispatchers.Main) {
//                    val startPoint = trackPoints[0]
//                    Timber.d("startPoint ${startPoint.lat}, ${startPoint.lon}")
//                    val cameraUpdate = CameraUpdateFactory.newCenterPosition(
//                        LatLng.from(
//                            startPoint.lat,
//                            startPoint.lon
//                        )
//                    )
                    kakaoMap?.let { map ->
                        Timber.d("addCourseLine")
//                        map.moveCamera(cameraUpdate)
                        addCourseLine(it, map, trackPoints)
                    }
                }
            }
        }
    }


    private fun findUserIdForLabel(label: Label): String? {
        for ((userId, storedLabel) in userLabels) {
            if (storedLabel == label) {
                return userId
            }
        }
        return null
    }

    private fun setCameraUpdateAndAddMarker(state: UserLocationState.Exist) {
        val cameraUpdate = CameraUpdateFactory.newCenterPosition(
            LatLng.from(
                state.locations.last().latitude,
                state.locations.last().longitude
            )
        )
        kakaoMap?.moveCamera(cameraUpdate)
        val userProfileImg = mainViewModel.profileImage.value
        viewModel.isUserLabel = true
        if (userProfileImg != null) {
            addMarker(
                state.locations.last().latitude,
                state.locations.last().longitude,
                mainViewModel.userId.value ?: "",
                userProfileImg
            )
        } else {
            addMarker(
                state.locations.last().latitude,
                state.locations.last().longitude,
                mainViewModel.userId.value ?: ""
            )
        }
    }


    // 클릭 시 정보를 보여주는 함수 (예: 이전 답변의 infoLabel 로직)
//    private fun showInfoForUser(kakaoMap: KakaoMap, position: LatLng, user:User) {
//        infoLabel?.remove() // 기존 정보 레이블 제거
//
//        val labelText = "유저: ${userInfo.name}\n상태: ${userInfo.status}"
//        // ... (정보 레이블 생성 및 표시 로직, 이전 답변 참고) ...
//        val infoLabelOptions = LabelOptions.from(position.latitude + 0.0001, position.longitude)
//            // ... 스타일 설정 ...
//            .setTexts(labelText)
//        infoLabel = kakaoMap.labelManager?.layer?.addLabel(infoLabelOptions)
//
//        // 클릭 시에는 자동으로 사라지게 하는 것보다, 다른 곳을 탭하거나 닫기 버튼을 누를 때 사라지게 하는 것이 일반적입니다.
//        // kakaoMap.setOnMapClickListener 등으로 처리 가능
//    }


}

