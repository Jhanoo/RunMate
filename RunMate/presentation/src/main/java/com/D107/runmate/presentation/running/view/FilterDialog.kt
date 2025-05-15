package com.D107.runmate.presentation.running.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.DialogFilterBinding
import com.D107.runmate.presentation.utils.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FilterDialog(private val setting: List<Int?>, private val callback: (List<Int?>) -> Unit): DialogFragment() {
    private var mContext: Context? = null
    private lateinit var binding: DialogFilterBinding
    private val buttons by lazy {
        listOf(
            binding.btnDistance5,
            binding.btnDistance10,
            binding.btnDistanceHalf,
            binding.btnDistanceFull
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialog)
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val point = CommonUtils.getWindowSize(mContext!!)
        val deviceWidth = point.x
        params?.width = (deviceWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var selectedKm = setting[0]
        var isLiked = setting[1]

        when (selectedKm) {
            0 -> {
                binding.btnDistance5.isSelected = true
                binding.btnDistance5.setBackgroundResource(R.drawable.bg_course_distance_item_active)
                binding.btnDistance5.setTextColor(resources.getColor(R.color.white))
            }
            1 -> {
                binding.btnDistance10.isSelected = true
                binding.btnDistance10.setBackgroundResource(R.drawable.bg_course_distance_item_active)
                binding.btnDistance10.setTextColor(resources.getColor(R.color.white))
            }
            2 -> {
                binding.btnDistanceHalf.isSelected = true
                binding.btnDistanceHalf.setBackgroundResource(R.drawable.bg_course_distance_item_active)
                binding.btnDistanceHalf.setTextColor(resources.getColor(R.color.white))
            }
            3 -> {
                binding.btnDistanceFull.isSelected = true
                binding.btnDistanceFull.setBackgroundResource(R.drawable.bg_course_distance_item_active)
                binding.btnDistanceFull.setTextColor(resources.getColor(R.color.white))
            }
        }

        if(isLiked == 1) binding.ivCourseLike.setImageResource(R.drawable.ic_course_like)
        else binding.ivCourseLike.setImageResource(R.drawable.ic_course_like_inactive)

        buttons.forEach { button ->
            button.setOnClickListener {
                if (button.isSelected) {
                    button.isSelected = false
                    selectedKm = null
                    button.setBackgroundResource(R.drawable.bg_course_distance_item)
                    button.setTextColor(resources.getColor(R.color.text))
                } else {
                    buttons.forEach {
                        it.isSelected = false
                        it.setBackgroundResource(R.drawable.bg_course_distance_item)
                        it.setTextColor(resources.getColor(R.color.text))
                    }
                    button.isSelected = true
                    button.setBackgroundResource(R.drawable.bg_course_distance_item_active)
                    button.setTextColor(resources.getColor(R.color.white))
                    selectedKm = buttons.indexOf(button)
                }
            }
        }

        binding.ivCourseLike.setOnClickListener {
            if(isLiked == null) {
                binding.ivCourseLike.setImageResource(R.drawable.ic_course_like)
                isLiked = 1
            } else {
                binding.ivCourseLike.setImageResource(R.drawable.ic_course_like_inactive)
                isLiked = null
            }
        }

        binding.btnConfirmShort.setOnClickListener {
            val list = listOf<Int?>(selectedKm, isLiked)
            callback(list)
            dialog?.dismiss()
        }

        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnReset.setOnClickListener {
            callback(listOf(null, null))
            dialog?.dismiss()
        }
    }
}