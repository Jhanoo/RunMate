package com.D107.runmate.presentation.user.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentLoginBinding
import com.D107.runmate.presentation.user.viewmodel.JoinViewModel
import com.D107.runmate.presentation.user.viewmodel.LoginViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::bind,
    R.layout.fragment_login
) {
    private val viewModel: LoginViewModel by viewModels()
    private val joinViewModel: JoinViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // 로그인 상태 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginViewModel.LoginUiState.Initial -> {
                        // 초기 상태
                    }

                    is LoginViewModel.LoginUiState.Loading -> {
                        // 로딩 상태 - 프로그레스 표시
                        showLoading(true)
                    }

                    is LoginViewModel.LoginUiState.Success -> {
                        // 로그인 성공
                        showLoading(false)
                        navigateToMain()
                    }

                    is LoginViewModel.LoginUiState.Error -> {
                        // 로그인 실패
                        showLoading(false)
                        showToast(state.message)
                    }
                }
            }
        }

        // 이미 로그인한 상태인지 확인
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewModel.isLoggedIn.collectLatest { isLoggedIn ->
//                if (isLoggedIn) {
//                    navigateToMain()
//                }
//            }
//        }

        // 에러 메시지 관찰
        viewModel.emailError.observe(viewLifecycleOwner) { errorMsg ->
            binding.emailInputText.error = errorMsg
        }

        viewModel.passwordError.observe(viewLifecycleOwner) { errorMsg ->
            binding.passwordInputText.error = errorMsg
        }
    }

    private fun setupListeners() {
        binding.loginBtn.setOnClickListener {
            val email = binding.emailInputText.text.toString()
            val password = binding.passwordInputText.text.toString()

            viewModel.login(email, password)
        }

        binding.joinBtn.setOnClickListener {
            joinViewModel.resetState()
            findNavController().navigate(R.id.JoinFragment)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // 로딩 표시 로직 구현
        binding.loginBtn.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        try {
            // 현재 목적지 ID 확인
            val currentDestId = findNavController().currentDestination?.id

            // 현재 loginFragment에 있는 경우에만 네비게이션 수행
            if (currentDestId == R.id.loginFragment) {
                findNavController().navigate(R.id.action_loginFragment_to_runningFragment)
            } else {
                Timber.d("이미 다른 화면에 있습니다: $currentDestId")
            }
        } catch (e: Exception) {
            Timber.e(e, "네비게이션 오류: ${e.message}")
        }
    }
    override fun onDestroyView() {
        viewModel.resetState()
        super.onDestroyView()
    }
}