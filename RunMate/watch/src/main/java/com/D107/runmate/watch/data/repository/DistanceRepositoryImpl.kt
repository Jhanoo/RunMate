package com.D107.runmate.watch.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.D107.runmate.watch.domain.model.Distance
import com.D107.runmate.watch.domain.repository.DistanceRepository
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DistanceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DistanceRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _distanceFlow = MutableStateFlow(Distance(0.0))
    private var totalDistance = 0.0
    private var lastLocation: Location? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                lastLocation?.let { last ->
                    val distance = last.distanceTo(location)
                    totalDistance += distance / 1000.0  // 미터를 킬로미터로 변환하면서 더하기
                    Log.d(
                        "distance",
                        "New distance: %.4f m, Total: %.4f km".format(distance, totalDistance)
                    )
                    _distanceFlow.tryEmit(Distance(totalDistance))
                }
                lastLocation = location
            }
        }

//        private val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                for (location in locationResult.locations) {
//                    // 정확도가 낮은 위치는 무시
//                    if (location.accuracy > 50) continue
//
//                    lastLocation?.let { last ->
//                        val distanceInMeters = last.distanceTo(location)
//
//                        // 너무 작은 움직임은 무시 (1미터 미만)
//                        if (distanceInMeters < 1.0) return@let
//
//                        totalDistance += distanceInMeters / 1000.0
//                        Log.d("distance", "New distance: %.4f m, Total: %.4f km".format(distanceInMeters, totalDistance))
//                        _distanceFlow.tryEmit(Distance(totalDistance))
//                    }
//                    lastLocation = location
//                }
//            }
//        }
    }

    @SuppressLint("MissingPermission")
    override fun getDistanceFlow(): Flow<Distance> = _distanceFlow.asStateFlow()

    @SuppressLint("MissingPermission")
    override suspend fun startDistanceMonitoring() {
        lastLocation = null


        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)  // 1초마다 요청
            .setMinUpdateIntervalMillis(500)  // 최소 0.5초 간격
//            .setMinUpdateDistanceMeters(2f)   // 최소 2미터 이동시 업데이트
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
    }

    override suspend fun stopDistanceMonitoring() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun resetDistance() {
        totalDistance = 0.0
        lastLocation = null
        _distanceFlow.value = Distance(0.0)
    }
}