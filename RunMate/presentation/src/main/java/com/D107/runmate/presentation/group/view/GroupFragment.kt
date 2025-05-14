package com.D107.runmate.presentation.group.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupBinding
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class GroupFragment : BaseFragment<FragmentGroupBinding>(
    FragmentGroupBinding::bind,
    R.layout.fragment_group
) {

    val viewModel: GroupViewModel by activityViewModels()
    val groupCreateViewModel: GroupCreateViewModel by activityViewModels()
    val navigateOptions = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(R.id.groupFragment, true)
        .build()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGroupJoinDialogListener()
        setClickListener()
        observeViewModel()
    }

    fun observeViewModel(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is GroupUiEvent.GoToGroupInfo -> {
                                findNavController().navigate(R.id.action_groupFragment_to_groupInfoFragment,null,navigateOptions)
                            }

                            is GroupUiEvent.GoToGroupRunning ->{
                                findNavController().navigate(R.id.groupRunningFragment,null,navigateOptions)
                            }

                            is GroupUiEvent.ShowToast -> {
                                showToast(event.message)
                            }

                            is GroupUiEvent.ToggleGroupFragmentVisible->{
                                Timber.d("group ui visible ${event.visible}")
                                if(event.visible){
                                    binding.groupFragmentLayout.visibility = View.VISIBLE
                                }else{
                                    binding.groupFragmentLayout.visibility = View.GONE
                                }

                            }
                            else -> {}

                        }
                    }
                }

            }
        }
    }

    private fun setupGroupJoinDialogListener() {
        parentFragmentManager.setFragmentResultListener(
            GroupJoinDialogFragment.REQUEST_KEY, // 요청 키
            this // LifecycleOwner (Fragment 자신)
        ) { requestKey, bundle ->
            if (requestKey == GroupJoinDialogFragment.REQUEST_KEY) {
                val invitationCode = bundle.getString(GroupJoinDialogFragment.RESULT_KEY_INVITATION_CODE)
                if (invitationCode != null) {
                    viewModel.submitInvitationCode(invitationCode)
                }
            }
        }
    }

    private fun setClickListener() {

        binding.btnCreateGroup.setOnClickListener{
            groupCreateViewModel.clearSelectedData()
            findNavController().navigate(R.id.action_groupFragment_to_groupCreateFragment)
        }
        binding.btnJoinGroup.setOnClickListener{
            GroupJoinDialogFragment.newInstance()
                .show(parentFragmentManager, GroupJoinDialogFragment.TAG)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCurrentGroup()
    }

    override fun onPause() {
        super.onPause()
//        binding.groupFragmentLayout.visibility = View.GONE
    }

}