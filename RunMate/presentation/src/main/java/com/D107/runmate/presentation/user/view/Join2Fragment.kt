package com.D107.runmate.presentation.user.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentPersonalJoinBinding
import com.D107.runmate.presentation.user.viewmodel.JoinViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class Join2Fragment : BaseFragment<FragmentPersonalJoinBinding>(
    FragmentPersonalJoinBinding::bind,
    R.layout.fragment_personal_join
) {
    private val viewModel: JoinViewModel by activityViewModels()

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedGender = ""

    private val getImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                setProfileImage(uri)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signupState.collectLatest { state ->
                when (state) {
                    is JoinViewModel.SignupUiState.Initial -> {
                        Log.d("Join2Fragment", "State: Initial")
                    }
                    is JoinViewModel.SignupUiState.Loading -> {
                        Log.d("Join2Fragment", "State: Loading")
                        showLoading(true)
                    }
                    is JoinViewModel.SignupUiState.Success -> {
                        Log.d("Join2Fragment", "State: Success")
                        showLoading(false)
                        showToast("회원가입이 성공적으로 완료되었습니다.")

                        viewModel.resetState()

                        navigateToLogin()
                    }
                    is JoinViewModel.SignupUiState.Error -> {
                        Log.d("Join2Fragment", "State: Error - ${state.message}")
                        showLoading(false)
                        showToast(state.message)
                    }
                }
            }
        }

        // 프로필 이미지 관찰
        viewModel.profileImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                Glide.with(requireContext())
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImg)
            }
        }
    }

    private fun setupListeners() {
        binding.leftArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.profileUpload.setOnClickListener {
            openGallery()
        }

        binding.birthDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.genderButton.setOnClickListener {
            showGenderPicker()
        }

        binding.signupButton.setOnClickListener {
            val nickname = binding.idEditText.text.toString()
            val weightText = binding.weightText.text.toString()
            val heightText = binding.heightText.text.toString()

            if (nickname.isBlank()) {
                showToast("닉네임을 입력해주세요.")
                return@setOnClickListener
            }

            if (selectedYear == 0 || selectedMonth == 0 || selectedDay == 0) {
                showToast("생년월일을 선택해주세요.")
                return@setOnClickListener
            }

            if (selectedGender.isBlank()) {
                showToast("성별을 선택해주세요.")
                return@setOnClickListener
            }

            if (weightText.isBlank()) {
                showToast("체중을 입력해주세요.")
                return@setOnClickListener
            }

            if (heightText.isBlank()) {
                showToast("키를 입력해주세요.")
                return@setOnClickListener
            }

            val weight = weightText.toDoubleOrNull()
            if (weight == null) {
                showToast("올바른 체중을 입력해주세요.")
                return@setOnClickListener
            }

            val height = heightText.toDoubleOrNull()
            if (height == null) {
                showToast("올바른 키를 입력해주세요.")
                return@setOnClickListener
            }

            if (weight <= 20 || weight > 300) {
                showToast("체중을 다시 확인해주세요.")
                return@setOnClickListener
            }

            if (height <= 100 || height > 350) {
                showToast("키를 다시 확인해주세요")
                return@setOnClickListener
            }

            viewModel.setNickname(nickname)
            viewModel.setBirthday(selectedYear, selectedMonth, selectedDay)
            viewModel.setGender(selectedGender)
            viewModel.setWeight(weight)
            viewModel.setHeight(height)

            viewModel.signup()
        }
    }

    private fun showDatePicker() {
        CustomDatePicker(
            initialYear = selectedYear,
            initialMonth = selectedMonth,
            initialDay = selectedDay,
            onDateSelected = { year, month, day, formattedDate ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                binding.birthDateButton.text = formattedDate
                binding.birthDateButton.setTextColor(Color.BLACK)
            }
        ).show(parentFragmentManager, "datePicker")
    }

    private fun showGenderPicker() {
        CustomGenderPicker(
            context = requireContext(),
            initialGender = selectedGender,
            onGenderSelected = { gender ->
                selectedGender = gender
                binding.genderButton.text = gender
                binding.genderButton.setTextColor(Color.BLACK)
            }
        ).show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageLauncher.launch(intent)
    }

    private fun setProfileImage(uri: Uri) {
        try {
            // URI를 ViewModel에 저장
            viewModel.setProfileImage(uri)

            // Glide를 사용하여 원형으로 표시
            Glide.with(requireContext())
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImg)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("이미지를 처리하는데 실패했습니다.")
            Log.e("Join2Fragment", "이미지 처리 오류", e)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // 로딩 표시 로직 구현
        binding.signupButton.isEnabled = !isLoading
    }

    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.loginFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}