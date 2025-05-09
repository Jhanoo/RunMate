package com.D107.runmate.presentation.login

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.navigation.fragment.findNavController
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.components.CustomDatePicker
import com.D107.runmate.presentation.components.CustomGenderPicker
import com.D107.runmate.presentation.databinding.FragmentJoin2Binding
import com.ssafy.locket.presentation.base.BaseFragment

class Join2Fragment : BaseFragment<FragmentJoin2Binding>(
    FragmentJoin2Binding::bind,
    R.layout.fragment_join2
) {
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedGender = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leftArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.birthDateButton.apply {
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        showDatePicker()
                        true
                    }
                    else -> false
                }
            }
        }

        binding.genderButton.apply {
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        showGenderPicker()
                        true
                    }
                    else -> false
                }
            }
        }

        binding.signupButton.setOnClickListener {
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    private fun showDatePicker() {
        CustomDatePicker(
            context = requireContext(),
            initialYear = selectedYear,
            initialMonth = selectedMonth,
            initialDay = selectedDay,
            onDateSelected = { year, month, day, formattedDate ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                binding.birthDateButton.text = formattedDate
                binding.birthDateButton.setTextColor(Color.BLACK)
            }
        ).show()
    }

    private fun showGenderPicker() {
        CustomGenderPicker(
            context = requireContext(),
            initialGender = selectedGender,
            onGenderSelected = { gender ->
                selectedGender = gender
                binding.genderButton.text = gender
                binding.genderButton.setTextColor(Color.BLACK)
            }
        ).show()
    }
}