package com.D107.runmate.presentation

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.D107.runmate.domain.model.running.LocationModel
import com.D107.runmate.domain.model.running.UserLocationState
import com.D107.runmate.presentation.databinding.ActivityMainBinding
import com.D107.runmate.presentation.databinding.DrawerHeaderBinding
import com.D107.runmate.presentation.manager.viewmodel.CurriculumViewModel
import com.D107.runmate.presentation.utils.LocationUtils.getLocation
import com.D107.runmate.presentation.utils.PermissionChecker
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.locket.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener {

    private var mContext: Context? = null
    private val viewModel: MainViewModel by viewModels()

    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    viewModel.saveFcmToken(token)
                }
            }

        // 뒤로가기
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // 드로어가 열려 있으면 닫기
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController

                    // 현재 목적지가 시작 목적지(홈 화면)이거나 주요 메뉴 화면인 경우
                    if (navController.currentDestination?.id == navController.graph.startDestinationId
                        || navController.currentDestination?.id == R.id.runningFragment
                        || navController.currentDestination?.id == R.id.AIManagerFragment
                        || navController.currentDestination?.id == R.id.wearableFragment
                        || navController.currentDestination?.id == R.id.groupFragment
                        || navController.currentDestination?.id == R.id.historyFragment) {

                        // 백 버튼 두 번 클릭 처리
                        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
                            // 두 번째 클릭이 간격 내에 발생하면 앱 종료
                            finish()
                        } else {
                            // 첫 번째 클릭 시 토스트 메시지 표시
                            Toast.makeText(this@MainActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                            backPressedTime = System.currentTimeMillis()
                        }
                    } else {
                        // 그 외 화면에서는 이전 화면으로 이동
                        navController.navigateUp()
                    }
                }
            }
        })

        mContext = this

        observeUserInfo()
        initDrawerHeader()

        getKeyHash()

        // TODO 앱 초기 실행 시, 사용자 정보 서버로부터 가져와서 MainActivity의 ViewModel에 저장하기
        setDrawerWidth()

        checkPermission()

        setNotificationChannel()

        binding.btnMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 뒤로가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // 1) 드로어가 열려 있으면 닫기
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // 2) 기본 뒤로가기 동작 호출
                    // 콜백을 비활성화한 뒤, 다시 디스패처에 이벤트를 넘겨줌
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun observeUserInfo() {
        lifecycleScope.launch {
            viewModel.nickname.collect { nickname ->
                Timber.d("nickname 변경 감지: $nickname")
                if (!nickname.isNullOrEmpty()) {
                    // 드로어 헤더의 이름 업데이트
                    val headerView = binding.navView.getHeaderView(0)
                    val headerBinding = DrawerHeaderBinding.bind(headerView)
                    headerBinding.tvName.text = nickname
                }
            }
        }

        lifecycleScope.launch {
            viewModel.profileImage.collect { profileImageUrl ->
                val headerView = binding.navView.getHeaderView(0)
                val headerBinding = DrawerHeaderBinding.bind(headerView)

                if (!profileImageUrl.isNullOrEmpty()) {
                    // 프로필 이미지 로드
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_drawer_profile)
                        .error(R.drawable.ic_drawer_profile)
                        .circleCrop()
                        .into(headerBinding.ivProfile)
                } else {
                    // 기본 이미지 설정
                    headerBinding.ivProfile.setImageResource(R.drawable.ic_drawer_profile)
                }
            }
        }

        // 별도의 코루틴으로 로그인 상태 확인
        lifecycleScope.launch {
            viewModel.userId.collect { userId ->
                Timber.d("userId 변경 감지: $userId")
                val navController = (supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                    .navController
                val current = navController.currentDestination?.id ?: return@collect

                if (current == R.id.splashFragment) {
                    // 스플래시 화면일 때는 SplashFragment 에서 로직 처리
                    return@collect
                }

                if (userId.isNullOrEmpty()) {
                    Timber.d("userId가 없음 - 로그인 화면으로 이동")
                    navController.navigate(
                        R.id.loginFragment,
                        null,
                        NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(current, true)
                            .build()
                    )
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val destinationMap = mapOf(
            R.id.drawer_running to R.id.runningFragment,
            R.id.drawer_group to R.id.groupFragment,
            R.id.drawer_history to R.id.historyFragment,
            R.id.drawer_wearable to R.id.wearableFragment,
            R.id.drawer_manager to R.id.AIManagerFragment
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.currentDestination?.id?.let { currentDestinationId ->
            if (destinationMap[item.itemId] == currentDestinationId) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return false
            }

            val navigateOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(currentDestinationId, true)
                .build()

            when (item.itemId) {
                R.id.drawer_running -> {
                    binding.navView.menu.findItem(R.id.drawer_running).isChecked = true
                    navController.navigate(
                        R.id.runningFragment,
                        null,
                        navigateOptions
                    )
                }

                R.id.drawer_group -> {
                    binding.navView.menu.findItem(R.id.drawer_group).isChecked = true
                    navController.navigate(
                        R.id.groupFragment,
                        null,
                        navigateOptions
                    )
                }

                R.id.drawer_history -> {
                    binding.navView.menu.findItem(R.id.drawer_history).isChecked = true
                    navController.navigate(
                        R.id.historyFragment,
                        null,
                        navigateOptions
                    )
                }

                R.id.drawer_wearable -> {
                    binding.navView.menu.findItem(R.id.drawer_wearable).isChecked = true
                    navController.navigate(
                        R.id.wearableFragment,
                        null,
                        navigateOptions
                    )
                }

                R.id.drawer_manager -> {
                    binding.navView.menu.findItem(R.id.drawer_manager).isChecked = true

                    // 일단 로딩창은 표시하지 않고, 바로 API 요청
                    val curriculumViewModel = ViewModelProvider(this).get(CurriculumViewModel::class.java)

                    // null 관련 문제를 추적하기 위한 로그 추가
                    Timber.d("AI 매니저 메뉴 선택됨: 커리큘럼 조회 시작")

                    lifecycleScope.launch {
                        try {
                            // 기존 결과 초기화
                            curriculumViewModel.resetMyCurriculum()

                            // 커리큘럼 조회 API 호출
                            curriculumViewModel.getMyCurriculum()

                            // 응답 대기 (최대 1초)
                            var timeoutCounter = 0
                            var hasCurriculum = false

                            while (timeoutCounter < 10) {
                                delay(100)
                                timeoutCounter++

                                val curriculumResult = curriculumViewModel.myCurriculum.value
                                Timber.d("커리큘럼 조회 결과($timeoutCounter): $curriculumResult")

                                // Result 객체가 있고, 성공한 경우만 확인
                                if (curriculumResult != null) {
                                    val curriculum = curriculumResult.getOrNull()

                                    if (curriculum != null) {
                                        // 커리큘럼이 있으면 AIManagerFragment로 이동
                                        Timber.d("커리큘럼 확인됨: curriculumId=${curriculum.curriculumId}")
                                        hasCurriculum = true

                                        navController.navigate(
                                            R.id.AIManagerFragment,
                                            bundleOf("curriculumId" to curriculum.curriculumId),
                                            navigateOptions
                                        )
                                        break
                                    } else if (curriculumResult.isFailure) {
                                        // API 호출은 완료되었지만 커리큘럼이 없는 경우
                                        Timber.d("커리큘럼 조회 실패: ${curriculumResult.exceptionOrNull()?.message}")
                                        break
                                    }
                                }
                            }

                            // 커리큘럼이 없거나 API 호출 타임아웃인 경우
                            if (!hasCurriculum) {
                                Timber.d("커리큘럼 없음: AIManagerIntroFragment로 이동")
//                                showHamburgerBtn(navController)
                                navController.navigate(
                                    R.id.AIManagerIntroFragment,
                                    null,
                                    navigateOptions
                                )
                            }
                        } catch (e: Exception) {
                            // 예외 발생 시 로그 출력 및 IntroFragment로 이동
                            Timber.e("AI 매니저 접근 오류: ${e.message}")
//                            showHamburgerBtn(navController)
                            navController.navigate(
                                R.id.AIManagerIntroFragment,
                                null,
                                navigateOptions
                            )
                        }
                    }
                }

                R.id.drawer_logout -> {
                    binding.navView.menu.findItem(R.id.drawer_logout).isChecked = true
                    showLogoutConfirmDialog()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
//                    hideHamburgerBtn()
                    return true
                }

                else -> {}
            }
        }
        true
        showHamburgerBtn(navController)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutConfirmDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        val btnLogout = dialog.findViewById<Button>(R.id.btn_confirm_short)
        btnLogout.setOnClickListener {
            performLogout()
            hideHamburgerBtn()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun performLogout() {
        viewModel.logout()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        navController.navigate(
            R.id.loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    private val checker = PermissionChecker(this)

    private val runtimePermissions =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

    private val backgroundPermission = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private fun checkPermission() {
        if (!checker.checkPermission(this, runtimePermissions)) {
            checker.setOnGrantedListener {
                Log.d(TAG, "checkPermission: checkBack")
                checkBackgroundPermission()
            }
            checker.requestPermissionLauncher.launch(runtimePermissions)
            return
        }
        Log.d(TAG, "checkPermission: checkBack2")
        checkBackgroundPermission()
    }

    private fun checkBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!checker.checkPermission(this, backgroundPermission)) {
                checker.setOnGrantedListener {
                    onAllPermissionsGranted()
                }
                checker.requestPermissionLauncher.launch(backgroundPermission)
                return
            }
        }
        onAllPermissionsGranted()
    }

    private fun onAllPermissionsGranted() {
        lifecycleScope.launch {
            try {
                mContext?.let {
                    val location = getLocation(it, this@MainActivity)
                    if(viewModel.userLocation.value is UserLocationState.Initial) {
                        viewModel.setUserLocation(
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
                    Log.d(TAG, "onAllPermissionsGranted: location ${location}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "onAllPermissionsGranted: ${e.message}")
            }
        }
    }

    private fun initDrawerHeader() {
        binding.navView.setNavigationItemSelectedListener(this)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // 초기 메뉴 아이템(달리기) 선택 상태로 설정
        binding.navView.menu.findItem(R.id.drawer_running).isChecked = true
    }

    fun setNavigation(id:Int) {
//        binding.navView.menu.findItem(id).isChecked = true
        onNavigationItemSelected(binding.navView.menu.findItem(id))
    }
    private fun setDrawerWidth() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val drawerWidth = (screenWidth * 0.6).toInt()

        val params = binding.navView.layoutParams as DrawerLayout.LayoutParams

        params.width = drawerWidth

        binding.navView.layoutParams = params
    }

    private fun showHamburgerBtn(navController: NavController) {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Timber.d("Destination changed to: ${destination.label}, ID: ${destination.id}")

            val nickname = viewModel.nickname.value // 현재 닉네임 값 가져오기 (StateFlow 사용 가정)
            val isLoginRequiredDestination = destination.id !in listOf(R.id.splashFragment, R.id.loginFragment, R.id.JoinFragment, R.id.Join2Fragment)

            if (nickname.isNullOrEmpty() && isLoginRequiredDestination) {
                Timber.d("Nickname is null/empty and current destination requires login. Navigating to loginFragment.")
                if (controller.currentDestination?.id != R.id.loginFragment) {
                    controller.navigate(
                        R.id.loginFragment,
                        null,
                        NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(controller.graph.startDestinationId, false) // 시작점까지 팝 (inclusive=false)
                            .setPopUpTo(destination.id, true) // 현재 목적지 팝 (inclusive=true)
                            .build()
                    )
                }
            }
            binding.btnMenu.visibility = when (destination.id) {
                R.id.goalSettingFragment -> View.VISIBLE
                R.id.runningFragment -> View.VISIBLE
                R.id.groupFragment -> View.VISIBLE
                R.id.historyFragment -> View.VISIBLE
                R.id.wearableFragment -> View.VISIBLE
                R.id.AIManagerFragment -> View.VISIBLE
                R.id.groupInfoFragment -> View.VISIBLE
                R.id.groupRunningFragment -> View.VISIBLE
                R.id.AIManagerIntroFragment -> View.VISIBLE
                R.id.splashFragment -> View.GONE
                R.id.loginFragment -> View.GONE
                R.id.JoinFragment -> View.GONE
                R.id.Join2Fragment -> View.GONE
                else -> View.GONE
            }
        }
    }

    fun hideHamburgerBtn() {
        binding.btnMenu.visibility = View.GONE
    }

    fun showHamburgerBtn() {
        binding.btnMenu.visibility = View.VISIBLE
    }


    fun getKeyHash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = this.packageManager.getPackageInfo(
                this.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            for (signature in packageInfo.signingInfo!!.apkContentsSigners) {
                try {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d(
                        "getKeyHash",
                        "key hash: ${Base64.encodeToString(md.digest(), Base64.NO_WRAP)}"
                    )
                } catch (e: NoSuchAlgorithmException) {
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature=$signature", e)
                }
            }
        }
    }



    fun setNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "GROUP_RUN_TERMINATION_CHANNEL"
            val channelName = "그룹 달리기 종료"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "그룹장이 그룹 달리기를 종료했을 때 알림을 받습니다."
                // 필요하다면 진동, 소리 등 설정
                // enableVibration(true)
                // setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // 새 인텐트로 교체
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.handleDeepLink(intent) // NavController에게 딥링크 처리 명시적 요청
    }
}