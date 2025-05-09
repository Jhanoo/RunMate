package com.D107.runmate.presentation.group.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentGroupInfoBinding
import com.D107.runmate.presentation.group.adapter.GroupMemberAdapter
import com.D107.runmate.presentation.group.viewmodel.GroupViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupInfoFragment : BaseFragment<FragmentGroupInfoBinding>(
    FragmentGroupInfoBinding::bind,
    R.layout.fragment_group_info) {

    private lateinit var adapter: GroupMemberAdapter
    private val viewModel: GroupViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setClickListener()
    }

    private fun setClickListener() {
        binding.btnInvinteGroupInfo.setOnClickListener{
            GroupInviteCodeFragment.newInstance("RUNMATECODE").show(parentFragmentManager, "GroupInviteCodeDialogTag")
        }
    }


    private fun setupRecyclerView() {
        binding.rvGroupMembers.layoutManager = GridLayoutManager(requireContext(), 3)

        adapter = GroupMemberAdapter()
        binding.rvGroupMembers.adapter = adapter

//        adapter.submitList()
    }

}