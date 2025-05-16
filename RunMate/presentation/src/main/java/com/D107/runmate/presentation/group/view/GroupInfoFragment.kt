package com.D107.runmate.presentation.group.view

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupInfoBinding
import com.D107.runmate.presentation.group.adapter.GroupMemberAdapter
import com.D107.runmate.presentation.group.viewmodel.GroupUiEvent
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
import com.D107.runmate.presentation.utils.CommonUtils
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class GroupInfoFragment : BaseFragment<FragmentGroupInfoBinding>(
    FragmentGroupInfoBinding::bind,
    R.layout.fragment_group_info) {

    private lateinit var adapter: GroupMemberAdapter
    private val viewModel: GroupViewModel by activityViewModels()
    val navigateOptions = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setPopUpTo(R.id.groupInfoFragment, true)
        .build()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setClickListener()
        observeViewModel()
    }




    private fun setClickListener() {
        binding.btnInvinteGroupInfo.setOnClickListener{
            GroupInviteCodeFragment.newInstance(viewModel.currentGroup.value?.inviteCode?:"").show(parentFragmentManager, "GroupInviteCodeDialogTag")
        }
        binding.btnGroupDisperse.setOnClickListener{
            viewModel.leaveGroup()
        }
        binding.btnExitGroupInfo.setOnClickListener{
            viewModel.leaveGroup()
        }
        binding.btnStartGroupInfo.setOnClickListener {
            viewModel.startGroup()
        }
    }

    fun observeViewModel(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch{
                    viewModel.currentGroup.collect{group->
                        if(group!=null) {
                            adapter.submitList(group.members)
                            binding.tvGroupNameGroupInfo.text = group.groupName
                            binding.tvLocationGroupInfo.text = group.startLocation
                            binding.tvDateGroupInfo.text = CommonUtils.formatIsoDateToCustom(group.startTime )
                            group.courseName?.let{
                                binding.tvCourseNameGroupInfo.text = it
                                binding.btnCourseDetailGroupInfo.visibility = View.VISIBLE

                            }

                            if(true){
                                binding.btnStartGroupInfo.visibility = View.VISIBLE
                                binding.btnExitGroupInfo.visibility = View.GONE
                                binding.btnGroupDisperse.visibility = View.VISIBLE
                            }else{
                                binding.btnStartGroupInfo.visibility = View.GONE
                                binding.btnExitGroupInfo.visibility = View.VISIBLE
                                binding.btnGroupDisperse.visibility = View.GONE
                            }

                        }

                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {

                            is GroupUiEvent.ShowToast -> {
                                showToast(event.message)
                            }

                            is GroupUiEvent.GoToGroup -> {
                                findNavController().navigate(R.id.groupFragment,null,navigateOptions)
                            }

                            is GroupUiEvent.GoToGroupRunning -> {
                                findNavController().navigate(R.id.groupRunningFragment,null,navigateOptions)
                            }
                            else -> {}

                        }
                    }
                }

            }
        }
    }


    private fun setupRecyclerView() {
        binding.rvGroupMembers.layoutManager = GridLayoutManager(requireContext(), 3)

        adapter = GroupMemberAdapter(viewModel.currentGroup.value?.leaderId?:"")
        binding.rvGroupMembers.adapter = adapter

//        adapter.submitList()
    }

}