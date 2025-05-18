package com.D107.runmate.presentation.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.domain.model.history.History
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.ItemCourseBinding
import com.D107.runmate.presentation.databinding.ItemHistoryBinding
import com.D107.runmate.presentation.utils.CommonUtils
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToHMS
import com.D107.runmate.presentation.utils.CommonUtils.formatSecondsToMS
import com.bumptech.glide.Glide

class HistoryRVAdapter:
    ListAdapter<History, HistoryRVAdapter.CustomViewHolder>(CustomComparator) {
    private lateinit var context: Context
    lateinit var itemClickListener: ItemClickListener

    interface ItemClickListener {
        fun onClick(view: View, data: History, position: Int)
    }

    companion object CustomComparator : DiffUtil.ItemCallback<History>() {
        override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: History) {
            binding.tvType.text = if(item.groupName == null) "개인" else item.groupName
            binding.tvHistoryDate.text = CommonUtils.formatIsoDateToCustom(item.startTime)
            binding.tvHistoryDuration.text = if(item.duration >= 3600) {
                formatSecondsToHMS(item.duration)
            } else {
                formatSecondsToMS(item.duration)
            }

            binding.tvCourseName.text = if(item.courseName === null) "히스토리" else item.courseName
            binding.tvCourseStartLocation.text = item.location
            binding.tvHistoryDistance.text = context.getString(R.string.course_distance, item.myDistance)

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, item, adapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        context = parent.context
        val binding =
            ItemHistoryBinding.inflate(LayoutInflater.from(context), parent, false)

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
