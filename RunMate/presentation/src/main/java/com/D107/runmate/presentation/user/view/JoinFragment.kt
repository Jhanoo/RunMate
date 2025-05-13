package com.D107.runmate.presentation.user.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentIdPwJoinBinding
import com.D107.runmate.presentation.user.viewmodel.JoinViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

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

            viewModel.setEmail(email)
            viewModel.setPassword(password)

            if (viewModel.validatePasswordMatch(password, passwordConfirm)) {
                viewModel.goToNextStep()
                findNavController().navigate(R.id.action_joinFragment_to_join2Fragment)
            }
        }
    }
}