package com.D107.runmate.presentation.group.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.FragmentPlaceSearchBinding
import com.D107.runmate.presentation.group.adapter.PlaceAdapter
import com.D107.runmate.presentation.group.viewmodel.GroupCreateViewModel
import com.ssafy.locket.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PlaceSearchFragment : BaseFragment<FragmentPlaceSearchBinding>(
    FragmentPlaceSearchBinding::bind,
    R.layout.fragment_place_search
) {
    val viewModel:GroupCreateViewModel by activityViewModels()
    lateinit var placeAdapter: PlaceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.queryResult.collect {
                placeAdapter.updatePlaces(it)
                binding.tvNoResult.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setClickListener() {
        binding.toolbarPlaceSearch.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.ivMapSearchPlace.setOnClickListener{
            findNavController().navigate(R.id.placeSelectFragment)
        }

        binding.btnClearSearchPlace.setOnClickListener {
            binding.etSearchPlace.text.clear()
            viewModel.clearQuery()
            toggleClearButton(false)
        }

        toggleClearButton(false)

        binding.etSearchPlace.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                toggleClearButton(!s.isNullOrEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearchPlace.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun toggleClearButton(show: Boolean) {
        binding.btnClearSearchPlace.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun performSearch(query: String) {
        hideKeyboard()

        // ViewModel에 검색 요청
        viewModel.searchPlace(query)
    }

    private fun setupRecyclerView() {
        placeAdapter = PlaceAdapter()
        placeAdapter.onItemClickListener ={place->
            Timber.d("$place")
            viewModel.selectPlace(place)
            findNavController().popBackStack()
        }
        binding.rvSearchPlace.apply {
            adapter = placeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchPlace.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearResult()
    }


}