package com.D107.runmate.presentation.running.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.course.CourseInfo
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.ItemCourseBinding
import com.bumptech.glide.Glide

class CourseRVAdapter:
    ListAdapter<CourseInfo, CourseRVAdapter.CustomViewHolder>(CustomComparator) {
    private lateinit var context: Context
    lateinit var itemClickListener: ItemClickListener

    interface ItemClickListener {
        fun onClick(view: View, data: CourseInfo, position: Int)
    }

    companion object CustomComparator : DiffUtil.ItemCallback<CourseInfo>() {
        override fun areItemsTheSame(oldItem: CourseInfo, newItem: CourseInfo): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: CourseInfo, newItem: CourseInfo): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CourseInfo) {
            binding.tvCourseTitle.text = item.courseName
            binding.tvCourseOwner.text = item.creator.nickname
            binding.tvCourseAltitude.text = context.getString(R.string.course_altitude, item.avgElevation)
            binding.tvDistance.text = context.getString(R.string.course_distance, item.distance)
            binding.tvCourseLikeCnt.text = context.getString(R.string.course_like_cnt, item.likeCount)
            binding.tvCourseStartLocation.text = item.startLocation

            Glide.with(context)
                .load(item.creator.profileImage)
                .circleCrop()
                .into(binding.ivProfile)

            if(item.liked) {
                binding.ivCourseLike.setImageResource(R.drawable.ic_course_like)
            } else {
                binding.ivCourseLike.setImageResource(R.drawable.ic_course_like_inactive)
            }

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, item, adapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        context = parent.context
        val binding =
            ItemCourseBinding.inflate(LayoutInflater.from(context), parent, false)

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
