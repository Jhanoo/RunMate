package com.D107.runmate.presentation

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.D107.runmate.presentation.databinding.ActivityMainBinding
import com.D107.runmate.presentation.databinding.DrawerHeaderBinding
import com.google.android.material.navigation.NavigationView
import com.ssafy.locket.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getKeyHash()

        // TODO 앱 초기 실행 시, 사용자 정보 서버로부터 가져와서 MainActivity의 ViewModel에 저장하기
        setDrawerWidth()

        binding.navView.setNavigationItemSelectedListener(this)

        // 초기 메뉴 아이템(달리기) 선택 상태로 설정
        binding.navView.menu.findItem(R.id.drawer_running).isChecked = true

        val headerView = binding.navView.getHeaderView(0)
        val headerBinding = DrawerHeaderBinding.bind(headerView)

        headerBinding.ivProfile.setImageResource(R.drawable.ic_drawer_profile) // TODO 사용자 프로필로 변경, 없을 경우 ic_drawer_profile 사용
        headerBinding.tvName.text = "한아영"

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

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setDrawerWidth() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val drawerWidth = (screenWidth * 0.6).toInt()

        val params = binding.navView.layoutParams as DrawerLayout.LayoutParams

        params.width = drawerWidth

        binding.navView.layoutParams = params
    }

    fun getKeyHash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            for (signature in packageInfo.signingInfo!!.apkContentsSigners) {
                try {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d("getKeyHash", "key hash: ${Base64.encodeToString(md.digest(), Base64.NO_WRAP)}")
                } catch (e: NoSuchAlgorithmException) {
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature=$signature", e)
                }
            }
        }
    }
}