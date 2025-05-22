package com.D107.runmate.presentation.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

private const val TAG = "PermissionChecker"
fun interface OnGrantedListener {
    fun onGranted()
}
class PermissionChecker(activityOrFragment: Any) {
    private lateinit var context:Context

    private lateinit var permitted : OnGrantedListener
    fun setOnGrantedListener(listener: OnGrantedListener){
        permitted = listener
    }

    fun checkPermission(context: Context, permissions: Array<String>): Boolean {
        this.context = context
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED ) {
                return false
            }
        }
        return true
    }

    val requestPermissionLauncher: ActivityResultLauncher<Array<String>> = when (activityOrFragment) {
        is AppCompatActivity -> {
            activityOrFragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()){
                resultChecking(it)
            }
        }

        is Fragment -> {
            activityOrFragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()){
                resultChecking(it)
            }
        }

        else -> {
            throw RuntimeException("Activity혹은 Fragment에서 권한설정이 가능합니다.")
        }
    }


    private fun resultChecking(result: Map<String, Boolean>) {
        if(result.values.contains(false)){
            result.keys.forEach { Log.d(TAG, "checkPermission: $it") }
//            moveToSettings()
        } else {
            permitted.onGranted()
        }
    }

    private fun moveToSettings() {
        val alertDialog = AlertDialog.Builder( context )
        alertDialog.setTitle("권한이 필요합니다.")
        alertDialog.setMessage("설정으로 이동합니다.")
        alertDialog.setPositiveButton("확인") { dialogInterface, i -> // 안드로이드 버전에 따라 다를 수 있음.
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + context.packageName))
            context.startActivity(intent)
            dialogInterface.cancel()
        }
        alertDialog.setNegativeButton("취소") { dialogInterface, i -> dialogInterface.cancel() }
        alertDialog.show()
    }
}



