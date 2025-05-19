package com.D107.runmate.presentation.history.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.history.History
import com.D107.runmate.presentation.MainViewModel
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.course.adapter.CourseRVAdapter
import com.D107.runmate.presentation.course.view.CourseSearchFragmentDirections
import com.D107.runmate.presentation.databinding.FragmentHistoryBinding
import com.D107.runmate.presentation.history.HistoryDetailState
import com.D107.runmate.presentation.history.HistoryListState
import com.D107.runmate.presentation.history.HistoryViewModel
import com.D107.runmate.presentation.history.adapter.HistoryRVAdapter
import com.D107.runmate.presentation.running.CourseSearchState
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::bind,
    R.layout.fragment_history
) {
    private lateinit var historyRVAdapter: HistoryRVAdapter
    private val historyViewModel: HistoryViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var dialog: FilterDialog
    private var setting: List<Int?> =
        listOf(null, null) // 첫 번째 거는 선택된 아이템(거리)의 index(선택 x는 -1), 두 번째 거는 0, 1로 bool대신 int
    private val distanceFilterList = listOf(0.0, 5.0, 10.0, 21.097, 120.0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()

        historyViewModel.getHistoryList()

        viewLifecycleOwner.lifecycleScope.launch {
            historyViewModel.historyList.collectLatest { state ->
                when (state) {
                    is HistoryListState.Success -> {
                        historyRVAdapter.submitList(state.historyInfo.histories)
                    }

                    is HistoryListState.Error -> {
                        Timber.d("getHistoryList Error {${state.message}}")
                    }

                    is HistoryListState.Initial -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            historyViewModel.historyDetail.collectLatest { state ->
                when (state) {
                    is HistoryDetailState.Success -> {
                        mainViewModel.userId.value?.let {
                            val action = HistoryFragmentDirections.actionHistoryFragmentToPersonalHistoryFragment(it, state.historyDetail.historyId)
                            findNavController().navigate(action)
                            historyViewModel.resetHistoryDetail()
                        }
                    }
                    is HistoryDetailState.Error -> {
                        Timber.d("getHistoryDetail Error {${state.message}}")
                    }
                    is HistoryDetailState.Initial -> {}
                }
            }
        }

        binding.btnFilter.setOnClickListener {
            dialog = FilterDialog(1, setting) { value ->
                if (value.size != 0) {
                    val historyList = historyViewModel.historyList.value
                    if (historyList is HistoryListState.Success) {
                        val minDistance =
                            if (value[0] == null) null else distanceFilterList[value[0]!!]
                        val maxDistance =
                            if (value[0] == null) null else if (value[0] == distanceFilterList.size - 1) null else distanceFilterList[value[0]!! + 1]
                        if (value[1] == 0) { // 전체
                            if (minDistance == null && maxDistance == null) {
                                historyRVAdapter.submitList(historyList.historyInfo.histories){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            } else if (minDistance != null && maxDistance == null) {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance > minDistance
                                    }
                                ){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            } else {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance >= minDistance!! && it.myDistance < maxDistance!!
                                    }
                                ){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            }
                        } else if (value[1] == 1) { // 그룹
                            if (minDistance == null && maxDistance == null) {
                                historyRVAdapter.submitList(historyList.historyInfo.histories.filter { it.groupName != null }){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            } else if (minDistance != null && maxDistance == null) {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance > minDistance && it.groupName != null
                                    }
                                ){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            } else {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance >= minDistance!! && it.myDistance < maxDistance!! && it.groupName != null
                                    }
                                ){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            }
                        } else { // 개인
                            if (minDistance == null && maxDistance == null) {
                                historyRVAdapter.submitList(historyList.historyInfo.histories.filter { it.groupName == null }){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            } else if (minDistance != null && maxDistance == null) {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance > minDistance && it.groupName == null
                                    }
                                ){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            } else {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance >= minDistance!! && it.myDistance < maxDistance!! && it.groupName == null
                                    }
                                ){
                                    binding.rvHistory.scrollToPosition(0)
                                }
                            }
                        }
                    }
                } else {
                    Timber.d("onViewCreated: value is empty")
                }
                setting = value
            }
            dialog.show(requireActivity().supportFragmentManager, "filter")
        }
    }

    private fun initAdapter() {
        historyRVAdapter = HistoryRVAdapter()

        binding.rvHistory.apply {
            adapter = historyRVAdapter
            layoutManager = LinearLayoutManager(requireContext())
            historyRVAdapter.submitList(listOf())
        }

        binding.rvHistory.itemAnimator = null

        historyRVAdapter.itemClickListener = object : HistoryRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: History, position: Int) {
                Timber.d("history ${data}")
                if(data.groupName == null) {
                    // 개인 기록화면으로 이동
                    historyViewModel.getHistoryDetail(data.historyId)
                } else {
                    // 그룹 기록화면으로 이동
                    val action = HistoryFragmentDirections.actionHistoryFragmentToGroupHistoryFragment(data.historyId)
                    findNavController().navigate(action)
                }
            }
        }
    }

}