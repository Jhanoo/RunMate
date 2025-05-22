package com.D107.runmate.presentation.group.view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.D107.runmate.presentation.base.BaseDialogFragment
import com.D107.runmate.presentation.databinding.FragmentGroupJoinDialogBinding

class GroupJoinDialogFragment : BaseDialogFragment<FragmentGroupJoinDialogBinding>(
    FragmentGroupJoinDialogBinding::inflate
) {

    companion object {
        const val TAG = "GroupJoinDialogFragment"
        const val REQUEST_KEY = "GroupJoinDialogRequest"
        const val RESULT_KEY_INVITATION_CODE = "invitationCode"

        fun newInstance(): GroupJoinDialogFragment {
            return GroupJoinDialogFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val code = binding.etInviteCode.text.toString().trim()
            if (code.isNotEmpty()) {
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY_INVITATION_CODE to code))
                dismiss()
            } else {
                binding.etInviteCode.error = "초대코드를 입력해주세요"
            }
        }


    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            val displayMetrics = requireContext().resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.9F).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT

            window.attributes = params
            window.setGravity(Gravity.CENTER)
        }
        dialog?.setCanceledOnTouchOutside(false)

    }

}