package com.D107.runmate.presentation.user.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.D107.runmate.domain.model.base.ResponseStatus
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentIdPwJoinBinding
import com.D107.runmate.presentation.user.viewmodel.JoinViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class JoinFragment : BaseFragment<FragmentIdPwJoinBinding>(
    FragmentIdPwJoinBinding::bind,
    R.layout.fragment_id_pw_join
) {
    private val viewModel: JoinViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.emailError.observe(viewLifecycleOwner) { errorMsg ->
            binding.idEditText.error = errorMsg
        }

        viewModel.passwordError.observe(viewLifecycleOwner) { errorMsg ->
            binding.pwEditText.error = errorMsg
        }

        viewModel.passwordConfirmError.observe(viewLifecycleOwner) { errorMsg ->
            binding.pwCheckEditText.error = errorMsg
        }
    }

    private fun setupListeners() {
        binding.leftArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.nextButton.setOnClickListener {
            val email = binding.idEditText.text.toString()
            val password = binding.pwEditText.text.toString()
            val passwordConfirm = binding.pwCheckEditText.text.toString()

            // 이메일, 비밀번호 기본 검증
            if (viewModel.validateEmail(email) && viewModel.validatePasswordMatch(password, passwordConfirm)) {
                lifecycleScope.launch {
                    try {
                        viewModel.checkEmailAvailability(email).collect { result ->
                            when (result) {
                                is ResponseStatus.Success -> {
                                    if (result.data) { // 사용 가능한 이메일
                                        viewModel.setEmail(email)
                                        viewModel.setPassword(password)
                                        viewModel.goToNextStep()
                                        findNavController().navigate(R.id.action_joinFragment_to_join2Fragment)
                                    } else {
                                        binding.idEditText.error = "이미 사용 중인 이메일입니다."
                                    }
                                }
                                is ResponseStatus.Error -> {
                                    Timber.e("Email check error: ${result.error.message}")
//                                    Toast.makeText(
//                                        requireContext(),
//                                        result.networkError.message,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e("Email check error: ${e.message}")
                        e.printStackTrace()
                        Toast.makeText(
                            requireContext(),
                            "오류가 발생했습니다: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}