package com.D107.runmate.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.D107.runmate.presentation.databinding.ActivityMainBinding
import com.D107.runmate.presentation.databinding.DrawerMainBinding
import com.ssafy.locket.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<DrawerMainBinding>(DrawerMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.mainInclude.mainBtn.setOnClickListener {
            binding.mainDrawerLayout.openDrawer(GravityCompat.START)
        }
    }
}