package com.D107.runmate.presentation.group.view

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupBinding
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFragment : BaseFragment<FragmentGroupBinding>(
    FragmentGroupBinding::bind,
    R.layout.fragment_group
) {




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGroupJoinDialogListener()
        setClickListener()
    }

    private fun setupGroupJoinDialogListener() {
        // GroupJoinDialogFragment로부터 결과를 받기 위한 리스너 설정
        parentFragmentManager.setFragmentResultListener(
            GroupJoinDialogFragment.REQUEST_KEY, // 요청 키
            this // LifecycleOwner (Fragment 자신)
        ) { requestKey, bundle ->
            if (requestKey == GroupJoinDialogFragment.REQUEST_KEY) {
                val invitationCode = bundle.getString(GroupJoinDialogFragment.RESULT_KEY_INVITATION_CODE)
                if (invitationCode != null) {
                    val navigateOptions = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.groupFragment, true)
                        .build()
                    findNavController().navigate(R.id.action_groupFragment_to_groupInfoFragment,null,navigateOptions)
                }
            }
        }
    }

    private fun setClickListener() {

        binding.btnCreateGroup.setOnClickListener{

            findNavController().navigate(R.id.action_groupFragment_to_groupCreateFragment)
        }
        binding.btnJoinGroup.setOnClickListener{
            GroupJoinDialogFragment.newInstance()
                .show(parentFragmentManager, GroupJoinDialogFragment.TAG)
        }
    }

}