package com.D107.runmate.presentation

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.D107.runmate.presentation.databinding.ActivityMainBinding
import com.D107.runmate.presentation.databinding.DrawerHeaderBinding
import com.D107.runmate.presentation.utils.PermissionChecker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.material.navigation.NavigationView
import com.ssafy.locket.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                    navController.navigate(
                        R.id.AIManagerFragment,
                        null,
                        navigateOptions
                    )
                }
            }
        }
        true
        hideHamburgerBtn(navController)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private val checker = PermissionChecker(this)

    private val runtimePermissions =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    private val backgroundPermission = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private fun checkPermission() {
        if (!checker.checkPermission(this, runtimePermissions)) {
            checker.setOnGrantedListener {
                checkBackgroundPermission()
            }
            checker.requestPermissionLauncher.launch(runtimePermissions)
            return
        }
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
                val location = getLocation()
                Log.d(TAG, "onAllPermissionsGranted: ${location.latitude} ${location.longitude}")
                viewModel.setUserLocation(UserLocationState.Exist(location))
            } catch (e: Exception) {
                Log.d(TAG, "onAllPermissionsGranted: ${e.message}")
            }
        }
    }

    // TODO GPS 설정 확인 추가
    private suspend fun getLocation(): Location = suspendCancellableCoroutine { cont ->
        val context = mContext ?: run {
            cont.resumeWithException(IllegalStateException("Context is null"))
            return@suspendCancellableCoroutine
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener { response ->
                Log.d(TAG, "getLocation: success")
                requestLocation(cont, context)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "getLocation: exception ${exception.message}")
                if (exception is ResolvableApiException) {
                    cont.resumeWithException(Exception("Location services are disabled. Please enable."))
                } else {
                    cont.resumeWithException(exception)
                }
            }
    }

    private fun requestLocation(
        cont: CancellableContinuation<Location>,
        context: Context
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location)
                } else {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            cont.resume(lastLocation ?: getFallbackLocation())
                        }
                }
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }


//    private suspend fun getLocation(): Location = suspendCancellableCoroutine { cont ->
//        mContext?.let {
//
//            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(it)
//
//            val locationManager = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                cont.resumeWithException(IllegalStateException("Location services are disabled"))
//                return@suspendCancellableCoroutine
//            }
//
//            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
//                .addOnSuccessListener { location: Location? ->
//                    if (location == null) {
//                        fusedLocationClient.lastLocation
//                            .addOnSuccessListener { lastLocation ->
//                                cont.resume(lastLocation ?: getFallbackLocation())
//                            }
//                            .addOnFailureListener {
//                                Log.d(TAG, "getLocation: ${it.message}")
//                                cont.resumeWithException(it)
//                            }
//                        Log.d(TAG, "getLocation: location is null")
//                    } else {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            try {
//                                mLocation = location
//                                Log.d(TAG, "getLocation: location ${location}")
//                                cont.resume(mLocation)
//                            } catch (e: Exception) {
//                                Log.d(TAG, "getLocation: exception ${e.message}")
//                                cont.resumeWithException(e)
//                            }
//                        }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    cont.resumeWithException(exception)
//                }
//        }
//
//    }

    private fun initDrawerHeader() {
        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.menu.findItem(R.id.drawer_running).isChecked = true

        // 초기 메뉴 아이템(달리기) 선택 상태로 설정
        onNavigationItemSelected(binding.navView.menu.findItem(R.id.drawer_running))
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val headerView = binding.navView.getHeaderView(0)
        val headerBinding = DrawerHeaderBinding.bind(headerView)

        headerBinding.ivProfile.setImageResource(R.drawable.ic_drawer_profile) // TODO 사용자 프로필로 변경, 없을 경우 ic_drawer_profile 사용
        headerBinding.tvName.text = "한아영"
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
                else -> View.GONE
            }
        }
    }

    private fun getFallbackLocation(): Location {
        val fallbackLocation = Location("fallback")
        fallbackLocation.latitude = 37.406960
        fallbackLocation.longitude = 127.115587
        return fallbackLocation
    }
}