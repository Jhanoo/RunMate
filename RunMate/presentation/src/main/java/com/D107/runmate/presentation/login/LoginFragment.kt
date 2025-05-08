package com.D107.runmate.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentHistoryBinding
import com.D107.runmate.presentation.databinding.FragmentLoginBinding
import com.ssafy.locket.presentation.base.BaseFragment


class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::bind,
    R.layout.fragment_login
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginBtn.setOnClickListener{
            findNavController().navigate(
                R.id.runningFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()
            )
        }

        binding.joinBtn.setOnClickListener {
            findNavController().navigate(R.id.JoinFragment)
        }
    }

}