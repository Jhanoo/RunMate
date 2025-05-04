package com.D107.runmate.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import com.D107.runmate.domain.model.Insole.InsoleSide
import com.D107.runmate.domain.model.Insole.SmartInsole
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class SmartInsoleMapper @Inject constructor() {

    fun map(scanResult: ScanResult): SmartInsole? {
        val address = scanResult.device.address
        if (address.isNullOrEmpty()) {
            return null
        }
        val name = scanResult.device.name
        val side = determineSideFromName(name) // 이름으로 좌/우 추정

        return SmartInsole(
            name = name,
            address = address,
            side = side
        )
    }

    private fun determineSideFromName(name: String?): InsoleSide {
        return when {
            name == null -> InsoleSide.UNKNOWN
            name.contains("_L", ignoreCase = true) || name.startsWith(
                "L_", ignoreCase = true
            ) || name.endsWith("_Left", ignoreCase = true) -> InsoleSide.LEFT

            name.contains("_R", ignoreCase = true) || name.startsWith(
                "R_", ignoreCase = true
            ) || name.endsWith("_Right", ignoreCase = true) -> InsoleSide.RIGHT

            else -> InsoleSide.UNKNOWN
        }
    }
}