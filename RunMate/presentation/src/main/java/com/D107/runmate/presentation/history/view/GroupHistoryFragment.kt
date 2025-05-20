package com.D107.runmate.presentation.history.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.history.GroupRun
import com.D107.runmate.domain.model.history.History
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.course.view.CourseDetailFragmentArgs
import com.D107.runmate.presentation.databinding.FragmentGroupHistoryBinding
import com.D107.runmate.presentation.history.HistoryDetailState
import com.D107.runmate.presentation.history.HistoryViewModel
import com.D107.runmate.presentation.history.adapter.GroupHistoryRVAdapter
import com.D107.runmate.presentation.history.adapter.HistoryRVAdapter
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToHMS
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToMS
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class GroupHistoryFragment : BaseFragment<FragmentGroupHistoryBinding>(
    FragmentGroupHistoryBinding::bind,
    R.layout.fragment_group_history
) {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
//    private val args: GroupHistoryFragmentArgs by navArgs()
    private lateinit var groupHistoryRVAdapter: GroupHistoryRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initUI()

//        historyViewModel.getHistoryDetail(args.historyId)
    }

    private fun initUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            historyViewModel.historyDetail.collectLatest { state ->
                when (state) {
                    is HistoryDetailState.Success -> {
                        binding.tvMyName.text = mainViewModel.nickname.value
                        binding.tvMyPace.text = getString(R.string.running_pace, (state.historyDetail.myRunItem.avgPace).toInt()/60, (state.historyDetail.myRunItem.avgPace).toInt()%60)
                        binding.tvMyDistance.text = getString(R.string.running_distance_int, state.historyDetail.myRunItem.distance.toInt())
                        binding.tvMyDuration.text = if(state.historyDetail.myRunItem.time >= 3600) {
                            formatSecondsToHMS(state.historyDetail.myRunItem.time.toInt())
                        } else {
                            formatSecondsToMS(state.historyDetail.myRunItem.time.toInt())
                        }
                        groupHistoryRVAdapter.submitList(state.historyDetail.groupRunItem)
                    }

                    is HistoryDetailState.Error -> {
                        Timber.d("getHistoryDetail Error {${state.message}}")
                    }

                    is HistoryDetailState.Initial -> {
                        Timber.d("getHistoryDetail Initial")
                    }
                }
            }
        }
    }

    private fun initAdapter() {
        groupHistoryRVAdapter = GroupHistoryRVAdapter()

        binding.rvGroupHistory.apply {
            adapter = groupHistoryRVAdapter
            layoutManager = LinearLayoutManager(requireContext())
            groupHistoryRVAdapter.submitList(listOf())
        }

        groupHistoryRVAdapter.itemClickListener = object : GroupHistoryRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: GroupRun, position: Int) {
                Timber.d("groupRun ${data}")
                // TODO 상세 화면 이동
                val historyDetail = historyViewModel.historyDetail.value
                if(historyDetail is HistoryDetailState.Success) {
                    historyViewModel.getGroupUserHistoryDetail(historyDetail.historyDetail.groupId!!, data.userId)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        historyViewModel.resetHistoryDetail()
    }
}