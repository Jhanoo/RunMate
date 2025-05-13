package com.D107.runmate.presentation.manager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.manager.ScheduleItem
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.ItemCheckListBinding

class ScheduleRVAdapter :
    ListAdapter<ScheduleItem, ScheduleRVAdapter.CustomViewHolder>(CustomComparator) {
    private lateinit var context: Context

    interface ItemClickListener {
        fun onClick(view: View, data: ScheduleItem, position: Int)
    }

    private var itemClickListener: ItemClickListener? = null

    fun setItemClickListener(listener: ItemClickListener) {
        this.itemClickListener = listener
    }

    companion object CustomComparator : DiffUtil.ItemCallback<ScheduleItem>() {
        override fun areItemsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(private val binding: ItemCheckListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkboxSchedule.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    // 클릭 이벤트 처리 (체크박스 상태 반영)
                    item.isCompleted = binding.checkboxSchedule.isChecked
                    itemClickListener?.onClick(it, item, position)
                }
            }
        }

        fun bind(item: ScheduleItem) {
            binding.tvDate.text = item.date
            binding.tvDay.text = item.day
            binding.checkboxSchedule.text = item.scheduleText
            binding.checkboxSchedule.isChecked = item.isCompleted
            item.colorIndicator?.let { binding.viewColorIndicator.setBackgroundColor(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        context = parent.context
        val binding =
            ItemCheckListBinding.inflate(LayoutInflater.from(context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}