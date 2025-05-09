package com.D107.runmate.presentation.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialogFragment<T : ViewBinding>(private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T) : DialogFragment() {

    private var _binding: T? = null
    protected val binding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        _binding = bindingInflater.invoke(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun Context.dialogFragmentResize(
        dialogFragment: DialogFragment,
        width: Float,
        height: Float
    ) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val window = dialogFragment.dialog?.window
        if (Build.VERSION.SDK_INT < 30) {
            val displayWidth = resources.displayMetrics.widthPixels
            val displayHeight = resources.displayMetrics.heightPixels

            val x = (displayWidth * width).toInt()
            val y = (displayHeight * height).toInt()

            window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()

            window?.setLayout(x, y)
        }
    }

    protected fun showKeyboard(editText: EditText) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun hideKeyboard(editText: EditText) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    protected fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}
