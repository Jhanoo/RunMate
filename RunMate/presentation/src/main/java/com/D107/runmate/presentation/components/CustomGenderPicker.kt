package com.D107.runmate.presentation.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.D107.runmate.presentation.R

class CustomGenderPicker(
    private val context: Context,
    private val initialGender: String = "",
    private val onGenderSelected: (gender: String) -> Unit
) {
    fun show() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_gender_picker)

        // 다이얼로그 배경을 투명하게 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 크기 설정
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        // 라디오 그룹과 버튼 초기화
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.genderRadioGroup)
        val maleRadio = dialog.findViewById<RadioButton>(R.id.maleRadioButton)
        val femaleRadio = dialog.findViewById<RadioButton>(R.id.femaleRadioButton)

        // 기존에 선택한 성별이 있으면 설정
        when (initialGender) {
            "남성" -> maleRadio.isChecked = true
            "여성" -> femaleRadio.isChecked = true
        }

        // 설정 버튼 클릭 리스너
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            val selectedGender = if (maleRadio.isChecked) "남성" else "여성"

            // 콜백 호출
            onGenderSelected(selectedGender)

            dialog.dismiss()
        }

        dialog.show()
    }
}