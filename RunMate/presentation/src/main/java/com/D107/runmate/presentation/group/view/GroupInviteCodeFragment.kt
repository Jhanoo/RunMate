package com.D107.runmate.presentation.group.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.base.BaseDialogFragment
import com.D107.runmate.presentation.databinding.FragmentGroupInviteCodeBinding

class GroupInviteCodeFragment : BaseDialogFragment<FragmentGroupInviteCodeBinding>(
    FragmentGroupInviteCodeBinding::inflate
)  {

    companion object {
        private const val ARG_INVITE_CODE = "invite_code"

        fun newInstance(inviteCode: String): GroupInviteCodeFragment {
            val fragment = GroupInviteCodeFragment()
            val args = Bundle()
            args.putString(ARG_INVITE_CODE, inviteCode)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inviteCodeFromArgs = arguments?.getString(ARG_INVITE_CODE)
        if (!inviteCodeFromArgs.isNullOrEmpty()) {
            binding.tvInviteCode.text = inviteCodeFromArgs
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.ivCopyInviteCode.setOnClickListener {
            copyInviteCodeToClipboard()
        }

        binding.btnShare.setOnClickListener {
            shareInviteCode()
        }
    }

    private fun copyInviteCodeToClipboard() {
        val inviteCode = binding.tvInviteCode.text.toString()
        if (inviteCode.isNotEmpty()) {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Invite Code", inviteCode)
            clipboard.setPrimaryClip(clip)
            showToastMessage("초대 코드가 복사되었습니다.")
        } else {
            showToastMessage("복사할 초대코드가 없습니다.")
        }
    }

    private fun shareInviteCode() {
        val inviteCode = binding.tvInviteCode.text.toString()
        if (inviteCode.isNotEmpty()) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "RunMate\n그룹에 초대되었습니다\n초대코드: $inviteCode")
                type = "text/plain"
            }
            try {
                startActivity(Intent.createChooser(shareIntent, "RunMate 그룹 초대코드 공유하기"))
            } catch (e: Exception) {
                showToastMessage("공유할 수 있는 앱이 설치되어 있지 않습니다.")
            }
        } else {
            showToastMessage("공유할 초대코드가 없습니다.")
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