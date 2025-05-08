package com.D107.runmate.presentation.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentJoinBinding
import com.ssafy.locket.presentation.base.BaseFragment

class JoinFragment : BaseFragment<FragmentJoinBinding>(
    FragmentJoinBinding::bind,
    R.layout.fragment_join
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideMainActivityMenuButton()

        binding.leftArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.nextButton.setOnClickListener{
            findNavController().navigate(R.id.action_joinFragment_to_join2Fragment)
        }
    }

    // 메뉴 버튼 지우기
    private fun hideMainActivityMenuButton() {
        (activity as? com.D107.runmate.presentation.MainActivity)?.let { mainActivity ->
            mainActivity.findViewById<View>(R.id.btn_menu)?.visibility = View.GONE
        }
    }
}