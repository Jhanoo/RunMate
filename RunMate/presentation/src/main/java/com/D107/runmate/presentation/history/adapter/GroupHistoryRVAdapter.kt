package com.D107.runmate.presentation.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.history.GroupRun
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.ItemGroupHistoryBinding
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToHMS
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToMS

class GroupHistoryRVAdapter: ListAdapter<GroupRun, GroupHistoryRVAdapter.CustomViewHolder> (CustomComparator) {
    private lateinit var context: Context
    lateinit var itemClickListener: ItemClickListener

    interface ItemClickListener {
        fun onClick(view: View, data: GroupRun, position: Int)
    }

    companion object CustomComparator : DiffUtil.ItemCallback<GroupRun>() {
        override fun areItemsTheSame(oldItem: GroupRun, newItem: GroupRun): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: GroupRun, newItem: GroupRun): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(private val binding: ItemGroupHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GroupRun) {
            binding.tvMyName.text = item.nickname
            binding.tvMyPace.text = context.resources.getString(R.string.running_pace, item.avgPace/60, item.avgPace%60)
            binding.tvMyDistance.text = context.resources.getString(R.string.running_distance_int, item.distance)
            binding.tvMyDuration.text = if(item.time >= 3600) {
                formatSecondsToHMS(item.time)
            } else {
                formatSecondsToMS(item.time)
            }

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, item, adapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        context = parent.context
        val binding =
            ItemGroupHistoryBinding.inflate(LayoutInflater.from(context), parent, false)

//        val displayMetrics = context.resources.displayMetrics
//        val screenHeight = displayMetrics.heightPixels
//        val itemHeight = (screenHeight * 0.26).toInt()
//        binding.root.layoutParams.height = itemHeight
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}
