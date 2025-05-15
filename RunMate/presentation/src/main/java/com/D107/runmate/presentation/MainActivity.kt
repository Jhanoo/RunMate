package com.D107.runmate.presentation

import android.Manifest
import android.app.Dialog
import android.content.Context
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
import com.D107.runmate.presentation.utils.LocationUtils.isEnableLocationSystem
import com.D107.runmate.presentation.utils.LocationUtils.showLocationEnableDialog
import com.D107.runmate.presentation.utils.PermissionChecker
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = this

        initDrawerHeader()
        // 사용자 정보 관찰
        observeUserInfo()

        getKeyHash()

        // TODO 앱 초기 실행 시, 사용자 정보 서버로부터 가져와서 MainActivity의 ViewModel에 저장하기
        setDrawerWidth()

        checkPermission()

        binding.btnMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun observeUserInfo() {
        lifecycleScope.launch {
            viewModel.nickname.collect { nickname ->
                if (!nickname.isNullOrEmpty()) {
                    // 드로어 헤더의 이름 업데이트
                    Timber.d("nickname : $nickname")
                    val headerView = binding.navView.getHeaderView(0)
                    val headerBinding = DrawerHeaderBinding.bind(headerView)
                    headerBinding.tvName.text = nickname
                }
                else {
                    Timber.d("delete nickname")
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController

                    navController.currentDestination?.id?.let { currentDestinationId ->


                        val navigateOptions = NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(currentDestinationId, true)
                            .build()

                        navController.navigate(R.id.loginFragment)
                    }
                }
            }
        }

        // 프로필 이미지 변경 관찰
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

                    // AI 매니저 메뉴 선택 시 커리큘럼 확인 로직
                    val curriculumViewModel = ViewModelProvider(this).get(CurriculumViewModel::class.java)

                    // 먼저 API 호출로 커리큘럼 존재 여부 확인
                    lifecycleScope.launch {
                        try {
                            // 커리큘럼 조회
                            curriculumViewModel.getMyCurriculum()

                            // 짧은 시간 대기 (API 응답 대기)
                            delay(300)

                            // 커리큘럼이 있는지 확인
                            val curriculum = curriculumViewModel.myCurriculum.value?.getOrNull()

                            if (curriculum != null) {
                                // 커리큘럼이 존재하면 AIManagerFragment로 이동
                                navController.navigate(
                                    R.id.AIManagerFragment,
                                    bundleOf("curriculumId" to curriculum.curriculumId),
                                    navigateOptions
                                )
                            } else {
                                // 커리큘럼이 없으면 인트로 화면으로 이동
                                navController.navigate(
                                    R.id.AIManagerIntroFragment,
                                    null,
                                    navigateOptions
                                )
                            }
                        } catch (e: Exception) {
                            // 오류 발생 시 인트로 화면으로 이동
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
                    hideHamburgerBtn()
                    return true
                }

                else -> {}
            }
        }
        true
        hideHamburgerBtn(navController)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutConfirmDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        val btnLogout = dialog.findViewById<Button>(R.id.btn_confirm_short)
        btnLogout.setOnClickListener {
            performLogout()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun performLogout() {
        viewModel.logout()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        navController.navigate(
//            R.id.loginFragment,
//            null,
//            NavOptions.Builder()
//                .setPopUpTo(R.id.nav_graph, true)
//                .build()
//        )
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
                    if (isEnableLocationSystem(it)) {
                        val location = getLocation(it)
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
                    } else {
                        showLocationEnableDialog(it)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "onAllPermissionsGranted: ${e.message}")
            }
        }
    }

    private fun initDrawerHeader() {
        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.menu.findItem(R.id.drawer_running).isChecked = true

        // 초기 메뉴 아이템(달리기) 선택 상태로 설정
//        onNavigationItemSelected(binding.navView.menu.findItem(R.id.drawer_running))
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val headerView = binding.navView.getHeaderView(0)
        val headerBinding = DrawerHeaderBinding.bind(headerView)

        headerBinding.ivProfile.setImageResource(R.drawable.ic_drawer_profile) // TODO 사용자 프로필로 변경, 없을 경우 ic_drawer_profile 사용
        headerBinding.tvName.text = "게스트"
    }

    private fun setDrawerWidth() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val drawerWidth = (screenWidth * 0.6).toInt()

        val params = binding.navView.layoutParams as DrawerLayout.LayoutParams

        params.width = drawerWidth

        binding.navView.layoutParams = params
    }

    private fun hideHamburgerBtn(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.btnMenu.visibility = when (destination.id) {
                R.id.goalSettingFragment -> View.VISIBLE
                R.id.runningFragment -> View.VISIBLE
                R.id.groupFragment -> View.VISIBLE
                R.id.historyFragment -> View.VISIBLE
                R.id.wearableFragment -> View.VISIBLE
                R.id.AIManagerFragment -> View.VISIBLE
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

    override fun onResume() {
        super.onResume()
//        checkPermission()
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
}