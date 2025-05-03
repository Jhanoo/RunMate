package com.D107.runmate.presentation.running

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.UserLocationState
import com.D107.runmate.presentation.databinding.FragmentRunningBinding
import com.D107.runmate.presentation.utils.CommonUtils.dpToPx
import com.bumptech.glide.Glide
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


private const val TAG = "RunningFragment"

@AndroidEntryPoint
class RunningFragment : BaseFragment<FragmentRunningBinding>(
    FragmentRunningBinding::bind,
    R.layout.fragment_running
) {
    private var kakaoMap: KakaoMap? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Log.d(TAG, "onMapError: ${p0?.message}")
            }

        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.userLocation.collectLatest { state ->
                when (state) {
                    is UserLocationState.Exist -> {
                        val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                            LatLng.from(
                                state.location.latitude,
                                state.location.longitude
                            )
                        )
                        kakaoMap?.moveCamera(cameraUpdate)
                        addMarker(state.location.latitude, state.location.longitude)
                    }

                    is UserLocationState.Initial -> {
                        Log.d(TAG, "onViewCreated: initial")
                    }
                }
            }
        }
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        mContext?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val profileUrl = "https://picsum.photos/200/300" // 테스트 이미지, 추후 사용자 프로필 이미지로 변경 예정
                val profileBitmap = getBitmapFromURL(profileUrl)
                profileBitmap?.let { bitmap ->
                    val markerBg =
                        BitmapFactory.decodeResource(
                            it.resources,
                            R.drawable.bg_running_marker_blue
                        )
                    val resizedMarkerBg =
                        Bitmap.createScaledBitmap(markerBg, dpToPx(it, 40f), dpToPx(it, 60f), true)

                    val combinedBitmap = createProfileMarker(resizedMarkerBg, bitmap)
                    val profilePng = bitmapToPng(combinedBitmap)

                    withContext(Dispatchers.Main) {
                        val styles = kakaoMap!!.labelManager
                            ?.addLabelStyles(LabelStyles.from(LabelStyle.from(profilePng)))
                        val options = LabelOptions.from(LatLng.from(latitude, longitude))
                            .setStyles(styles)
                        val layer = kakaoMap!!.labelManager!!.layer
                        val label = layer!!.addLabel(options)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
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
            val url = URL(src)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                doInput = true
                connect()
            }
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
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
}