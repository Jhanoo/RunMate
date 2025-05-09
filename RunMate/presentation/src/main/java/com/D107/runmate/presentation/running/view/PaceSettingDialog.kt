package com.D107.runmate.presentation.running.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.DialogPaceSettingBinding
import com.D107.runmate.presentation.utils.CommonUtils
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "PaceSettingDialog"
@AndroidEntryPoint
class PaceSettingDialog(private val setting: List<Int>, private val callback: (List<Int>) -> Unit): DialogFragment() {
    private var mContext: Context? = null
    private lateinit var binding: DialogPaceSettingBinding

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
        binding = DialogPaceSettingBinding.inflate(inflater, container, false)
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

        val secValues = Array(12) { (it * 5).toString() } // 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55

        binding.npMin.minValue = 0
        binding.npMin.maxValue = 20
        binding.npMin.wrapSelectorWheel = false
        binding.npMin.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        binding.npSec.minValue = 0
        binding.npSec.maxValue = secValues.size - 1
        binding.npSec.displayedValues = secValues
        binding.npSec.wrapSelectorWheel = false
        binding.npSec.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        if(setting.size != 0) {
            binding.npMin.value = setting[0]
            binding.npSec.value = setting[1]
            binding.groupValueExist.visibility = View.VISIBLE
            binding.btnConfirm.visibility = View.GONE
        } else {
            binding.npSec.value = 3
            binding.npMin.value = 6
            binding.groupValueExist.visibility = View.GONE
            binding.btnConfirm.visibility = View.VISIBLE
        }

        binding.npSec.setOnValueChangedListener { picker, oldVal, newVal ->
            val selectedValue = secValues[newVal].toInt()
            Log.d(TAG, "onViewCreated: selectedValue ${selectedValue}")
        }

        binding.btnConfirm.setOnClickListener {
            val list = listOf<Int>(binding.npMin.value, binding.npSec.value)
            callback(list)
            dialog?.dismiss()
        }

        binding.btnConfirmShort.setOnClickListener {
            val list = listOf<Int>(binding.npMin.value, binding.npSec.value)
            callback(list)
            dialog?.dismiss()
        }

        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnReset.setOnClickListener {
            callback(listOf())
            dialog?.dismiss()
        }
    }
}