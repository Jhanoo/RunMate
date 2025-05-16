package com.D107.runmate.presentation.group.view

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentPlaceSelectBinding
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.D107.runmate.presentation.utils.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class PlaceSelectFragment : BaseFragment<FragmentPlaceSelectBinding>(
    FragmentPlaceSelectBinding::bind,
    R.layout.fragment_place_select
) {

    val viewModel: GroupCreateViewModel by activityViewModels()

    private var kakaoMap: KakaoMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
        observeViewModel()
        setClickListeners()

    }

    private fun setClickListeners() {
        binding.btnConfirmSelectPlace.setOnClickListener {
            val currentAddress = binding.tvAddressSelectPlace.text.toString()
            if (currentAddress != "주소를 찾을 수 없습니다." && currentAddress.isNotBlank()) {
                kakaoMap?.cameraPosition?.position?.let { currentPosition ->
                    val latitude = currentPosition.latitude
                    val longitude = currentPosition.longitude
                    Timber.d("선택된 위치: 주소=$currentAddress, 위도=$latitude, 경도=$longitude")
                    viewModel.selectPlaceOnMap(currentAddress, latitude, longitude)
                    findNavController().popBackStack(R.id.groupCreateFragment, false)
                } ?: run {
                    showToast("지도 정보를 가져올 수 없습니다. 다시 시도해주세요.")
                }
            } else {
                showToast("유효한 주소를 선택해주세요.")
            }

        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.addressResult.collect{addressResult->
                        binding.tvAddressSelectPlace.text = addressResult?.address_name?:"주소를 찾을 수 없습니다."
                    }
                }
            }
        }
    }

    private fun initMap() {
        binding.mapViewSelectPlace.start(object : MapLifeCycleCallback(){
            override fun onMapDestroy() {
            }

            override fun onMapError(p0: Exception?) {
                Timber.e("onMapError : ${p0?.message}")
            }

        },object : KakaoMapReadyCallback(){
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0
                kakaoMap?.setOnCameraMoveEndListener { kakaoMap, cameraPosition, gestureType ->
                    viewModel.getCoord2Address(cameraPosition.position.longitude,cameraPosition.position.latitude)
                }
                getCurrentLocationAndMoveCamera()
            }

        }

        )
    }
    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndMoveCamera() {
        if(viewModel.selectedPlace.value!=null){
            viewModel.selectedPlace.value?.let{
                val selectedLatLng = LatLng.from(it.y, it.x)
                kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(selectedLatLng))
            }


        }else {
            if (!::fusedLocationClient.isInitialized) {
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireContext())
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLatLng = LatLng.from(location.latitude, location.longitude)
                        Timber.d("현재 위치 가져오기 성공: Lat=${currentLatLng.latitude}, Lng=${currentLatLng.longitude}")
                        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(currentLatLng))

                    }
                }
        }
    }


}