package com.D107.runmate.presentation.manager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.manager.ScheduleItem
import com.D107.runmate.presentation.databinding.ItemCheckListBinding

class ScheduleRVAdapter :
    ListAdapter<ScheduleItem, ScheduleRVAdapter.CustomViewHolder>(CustomComparator) {
    private lateinit var context: Context

    interface ItemClickListener {
        fun onClick(view: View, data: ScheduleItem, position: Int)
    }

    private var itemClickListener: ItemClickListener? = null

    private var selectedPosition = 1

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    fun setSelectedPosition(position: Int) {
        val previousSelected = selectedPosition
        selectedPosition = position

        // 이전에 선택된 아이템과 새로 선택된 아이템 갱신
        if (previousSelected != -1 && previousSelected < itemCount) {
            notifyItemChanged(previousSelected)
        }
        if (selectedPosition != -1 && selectedPosition < itemCount) {
            notifyItemChanged(selectedPosition)
        }
    }

    companion object CustomComparator : DiffUtil.ItemCallback<ScheduleItem>() {
        override fun areItemsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(val binding: ItemCheckListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // 체크박스 클릭 리스너
            binding.checkboxSchedule.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    // 클릭 이벤트 처리 (체크박스 상태 반영)
                    item.isCompleted = binding.checkboxSchedule.isChecked
                    itemClickListener?.onClick(it, item, position)
                }
            }

            // 텍스트 라벨 클릭 리스너 - 체크박스와 동일한 기능
            binding.checkboxLabel.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    // 체크박스 상태 토글
                    binding.checkboxSchedule.isChecked = !binding.checkboxSchedule.isChecked
                    item.isCompleted = binding.checkboxSchedule.isChecked
                    itemClickListener?.onClick(it, item, position)
                }
            }
        }

        fun bind(item: ScheduleItem) {
            binding.tvDate.text = item.date
            binding.tvDay.text = item.day

            // 체크박스에는 텍스트 없이 isCompleted 상태만 반영
            binding.checkboxSchedule.text = null
            binding.checkboxSchedule.isChecked = item.isCompleted ?: false

            // 일정 텍스트는 별도의 TextView에 설정
            binding.checkboxLabel.text = item.scheduleText

            if (adapterPosition == selectedPosition) {
                binding.viewColorIndicator.visibility = View.VISIBLE
            } else {
                binding.viewColorIndicator.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        context = parent.context
        val binding =
            ItemCheckListBinding.inflate(LayoutInflater.from(context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        if (position == selectedPosition) {
            holder.binding.viewColorIndicator.visibility = View.VISIBLE
        } else {
            holder.binding.viewColorIndicator.visibility = View.INVISIBLE
        }
    }
}