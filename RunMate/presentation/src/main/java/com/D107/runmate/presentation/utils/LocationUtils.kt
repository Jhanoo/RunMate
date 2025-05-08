package com.D107.runmate.presentation.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationUtils {
    private const val TAG = "LocationUtils"

    suspend fun getLocation(context: Context): Location = suspendCancellableCoroutine { cont ->
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(context)
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                Log.d(TAG, "Location settings are satisfied")
                fetchCurrentLocation(cont, context)
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    cont.resumeWithException(Exception("Location services disabled. Please enable."))
                } else {
                    cont.resumeWithException(exception)
                }
            }
    }

    private fun fetchCurrentLocation(
        cont: CancellableContinuation<Location>,
        context: Context
    ) {
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location, null)
                } else {
                    fetchLastKnownLocation(cont, context)
                }
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    private fun fetchLastKnownLocation(
        cont: CancellableContinuation<Location>,
        context: Context
    ) {
        LocationServices.getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { lastLocation ->
                cont.resume(lastLocation ?: getFallbackLocation(), null)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    fun trackingLocation(context: Context): Flow<Location> = callbackFlow {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    trySend(location)
                }
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    fun isEnableLocationSystem(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.isLocationEnabled!!
        }else{
            val mode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    fun getFallbackLocation(): Location {
        val fallbackLocation = Location("fallback")
        fallbackLocation.latitude = 37.406960
        fallbackLocation.longitude = 127.115587
        return fallbackLocation
    }

    fun showLocationEnableDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("위치 서비스 필요")
            .setMessage("정확한 위치 확인을 위해 GPS를 활성화해 주세요")
            .setPositiveButton("설정") { _, _ ->
                startLocationSettings(context)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    } // TODO UI 추후 수정

    private fun startLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "설정 화면을 열 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    fun getPaceFromSpeed(speed: Float): String {
        val minPerMs = 16.6667 / speed
        val min = minPerMs.toInt()
        val sec = ((minPerMs - min) * 60).toInt()
        Log.d(TAG, "startLocationTracking getPaceFromSpeed: $speed $minPerMs $sec")
        return "%d'%02d\"".format(min, sec)
    }
}