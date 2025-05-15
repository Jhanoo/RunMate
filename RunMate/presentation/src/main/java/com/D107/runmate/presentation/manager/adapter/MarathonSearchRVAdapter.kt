package com.D107.runmate.presentation.manager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.manager.MarathonInfo
import com.D107.runmate.presentation.databinding.ItemMarathonBinding

class MarathonSearchRVAdapter :
    ListAdapter<MarathonInfo, MarathonSearchRVAdapter.MarathonViewHolder>(MarathonDiffCallback) {

    private lateinit var context: Context

    // 아이템 클릭 리스너 인터페이스
    interface ItemClickListener {
        fun onClick(view: View, data: MarathonInfo, position: Int)
    }

    // 클릭 리스너 객체
    private var itemClickListener: ItemClickListener? = null

    // 클릭 리스너 설정 메서드
    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    companion object MarathonDiffCallback : DiffUtil.ItemCallback<MarathonInfo>() {
        override fun areItemsTheSame(oldItem: MarathonInfo, newItem: MarathonInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MarathonInfo, newItem: MarathonInfo): Boolean {
            return oldItem == newItem
        }
    }

    // ViewHolder 클래스
    inner class MarathonViewHolder(private val binding: ItemMarathonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // 아이템 클릭 이벤트 설정
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onClick(it, getItem(position), position)
                }
            }
        }

        fun bind(item: MarathonInfo) {
            // 날짜 및 요일 설정
            binding.tvDate.text = item.date // "6/21" 형식
            binding.tvDay.text = item.dayOfWeek // "일" 형식

            // 요일에 따라 텍스트 색상 변경 (일요일은 빨간색, 토요일은 파란색 등)
            when (item.dayOfWeek) {
                "일" -> binding.tvDay.setTextColor(context.getColor(android.R.color.holo_red_light))
                "토" -> binding.tvDay.setTextColor(context.getColor(android.R.color.holo_blue_light))
                else -> binding.tvDay.setTextColor(context.getColor(android.R.color.black))
            }

            // 마라톤 제목 및 위치 설정
            binding.tvMarathonTitle.text = item.title // "춘천 호반 마라톤" 형식
            binding.tvLocation.text = item.location // "강원, 춘천 송암스포츠타운 종합경기장" 형식
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarathonViewHolder {
        context = parent.context
        val binding = ItemMarathonBinding.inflate(LayoutInflater.from(context), parent, false)
        return MarathonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarathonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}