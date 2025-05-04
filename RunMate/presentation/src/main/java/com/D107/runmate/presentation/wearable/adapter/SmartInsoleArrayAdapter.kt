package com.D107.runmate.presentation.wearable.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.D107.runmate.domain.model.Insole.SmartInsole

class SmartInsoleArrayAdapter(
    context: Context,
    private val devices: MutableList<SmartInsole> // 내부에서 사용할 리스트
) : ArrayAdapter<SmartInsole>(context, android.R.layout.simple_list_item_1, devices) {

    // 리스트뷰에 표시될 텍스트를 반환하는 메서드 (toString 대신 사용할 수 있음)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        val device = getItem(position) // position에 해당하는 SmartInsole 객체 가져오기
        if (device != null) {
            textView.text = "${device.name ?: "Unknown"} (${device.address}) - ${device.side}"
        } else {
            textView.text = "Invalid device data" // 예외 처리
        }
        return view
    }

    // 데이터 업데이트 함수
    fun updateData(newDevices: List<SmartInsole>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged() // Adapter에 변경 사항 알림
    }
}