package com.D107.runmate.presentation.history.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.history.History
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.course.adapter.CourseRVAdapter
import com.D107.runmate.presentation.course.view.CourseSearchFragmentDirections
import com.D107.runmate.presentation.databinding.FragmentHistoryBinding
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
                                historyRVAdapter.submitList(historyList.historyInfo.histories)
                            } else if (minDistance != null && maxDistance == null) {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance > minDistance
                                    }
                                )
                            } else {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance >= minDistance!! && it.myDistance < maxDistance!!
                                    }
                                )
                            }
                        } else if (value[1] == 1) { // 그룹
                            if (minDistance == null && maxDistance == null) {
                                historyRVAdapter.submitList(historyList.historyInfo.histories.filter { it.groupName != null })
                            } else if (minDistance != null && maxDistance == null) {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance > minDistance && it.groupName != null
                                    }
                                )
                            } else {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance >= minDistance!! && it.myDistance < maxDistance!! && it.groupName != null
                                    }
                                )
                            }
                        } else {
                            if (minDistance == null && maxDistance == null) {
                                historyRVAdapter.submitList(historyList.historyInfo.histories.filter { it.groupName == null })
                            } else if (minDistance != null && maxDistance == null) {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance > minDistance && it.groupName == null
                                    }
                                )
                            } else {
                                historyRVAdapter.submitList(
                                    historyList.historyInfo.histories.filter {
                                        it.myDistance >= minDistance!! && it.myDistance < maxDistance!! && it.groupName == null
                                    }
                                )
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

        historyRVAdapter.itemClickListener = object : HistoryRVAdapter.ItemClickListener {
            override fun onClick(view: View, data: History, position: Int) {
                Timber.d("history ${data}")
                if(data.groupName == null) {
                    // 개인 기록화면으로 이동
                } else {
                    // 그룹 기록화면으로 이동
                    val action = HistoryFragmentDirections.actionHistoryFragmentToGroupHistoryFragment(data.historyId)
                    findNavController().navigate(action)
                }
            }
        }
    }

}