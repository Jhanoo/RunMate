package com.D107.runmate.presentation.login

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentIdPwJoinBinding
import com.ssafy.locket.presentation.base.BaseFragment

class JoinFragment : BaseFragment<FragmentIdPwJoinBinding>(
    FragmentIdPwJoinBinding::bind,
    R.layout.fragment_id_pw_join
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leftArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.nextButton.setOnClickListener{
            findNavController().navigate(R.id.action_joinFragment_to_join2Fragment)
        }
    }
}