package com.D107.runmate.presentation.group.adapter


import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.D107.runmate.domain.model.common.User
import com.D107.runmate.presentation.R
import com.D107.runmate.presentation.databinding.ItemGroupMemberBinding
import com.bumptech.glide.Glide


class GroupMemberAdapter(val leaderId:String) :
    ListAdapter<User, GroupMemberAdapter.ViewHolder>(GroupMemberDiffCallback()){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGroupMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemGroupMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: User) {
            binding.tvMemberName.text = member.nickname
            if(member.userId == leaderId){
                binding.ivGroupLeader.visibility = View.VISIBLE
            }else{
                binding.ivGroupLeader.visibility = View.GONE
            }

            if (!member.profileImage.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(member.profileImage)
                    .placeholder(R.drawable.tonie_round)
                    .error(R.drawable.tonie_round)
                    .circleCrop()
                    .into(binding.ivMemberProfile)
            }else{
                Glide.with(binding.root.context)
                    .load(R.drawable.tonie_round)
                    .placeholder(R.drawable.tonie_round)
                    .circleCrop()
                    .into(binding.ivMemberProfile)
            }
        }
    }

    class GroupMemberDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.memberId == newItem.memberId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

}