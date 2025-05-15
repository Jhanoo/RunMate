package com.D107.runmate.presentation.manager.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.domain.model.manager.MarathonInfo
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentAIManagerSearchMarathonBinding
import com.D107.runmate.presentation.manager.adapter.MarathonSearchRVAdapter
import com.D107.runmate.presentation.manager.viewmodel.MarathonViewModel
import com.ssafy.locket.presentation.base.BaseFragment

class AIManagerSearchMarathonFragment : BaseFragment<FragmentAIManagerSearchMarathonBinding>(
    FragmentAIManagerSearchMarathonBinding::bind,
    R.layout.fragment_a_i_manager_search_marathon
) {
    private val viewModel: MarathonViewModel by viewModels()
    private lateinit var marathonAdapter: MarathonSearchRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        // 초기 데이터 로드
        viewModel.getMarathons()
    }

    private fun setupToolbar() {
        binding.toolbarMarathonSearch.setNavigationOnClickListener {
            navigateBack()
        }
    }

    private fun setupRecyclerView() {
        marathonAdapter = MarathonSearchRVAdapter().apply {
            setItemClickListener(object : MarathonSearchRVAdapter.ItemClickListener {
                override fun onClick(view: View, data: MarathonInfo, position: Int) {
                    // 선택한 마라톤을 결과로 반환하고 이전 화면으로 돌아가기
                    setSelectedMarathonAndNavigateBack(data)
                }
            })
        }

        binding.rvMarathonSearch.apply {
            adapter = marathonAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchView() {
        // 검색 아이콘 클릭
        binding.ivSearchPlace.setOnClickListener {
            performSearch()
        }

        // 클리어 버튼 클릭
        binding.btnClearSearchMarathon.setOnClickListener {
            binding.etSearchMarathon.text.clear()
            viewModel.searchMarathons("")
        }

        // 검색창 텍스트 변경 리스너
        binding.etSearchMarathon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 텍스트가 비어있으면 모든 마라톤 표시, 비어있지 않으면 검색어에 맞게 필터링
                viewModel.searchMarathons(s.toString())
            }
        })

        // 키보드 검색 버튼 클릭 시
        binding.etSearchMarathon.setOnEditorActionListener { _, actionId, _ ->
            performSearch()
            true
        }
    }

    private fun performSearch() {
        val query = binding.etSearchMarathon.text.toString()
        viewModel.searchMarathons(query)
    }

    private fun observeViewModel() {
        viewModel.marathons.observe(viewLifecycleOwner) { marathons ->
            marathonAdapter.submitList(marathons)
        }
    }

    private fun setSelectedMarathonAndNavigateBack(marathon: MarathonInfo) {
        val result = Bundle().apply {
            putString("marathonId", marathon.id)
            putString("marathonTitle", marathon.title)
            putString("marathonDate", marathon.date)
            putString("marathonLocation", marathon.location)
            putStringArrayList("marathonDistances", ArrayList(marathon.distance))
        }

        parentFragmentManager.setFragmentResult("marathonSearchResult", result)
        navigateBack()
    }

    private fun navigateBack() {
        parentFragmentManager.popBackStack()
    }
}