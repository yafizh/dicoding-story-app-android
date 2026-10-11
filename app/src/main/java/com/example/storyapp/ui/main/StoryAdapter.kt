package com.example.storyapp.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.remote.response.ListStoryItem
import com.example.storyapp.databinding.ItemStoryBinding
import com.example.storyapp.utils.toFormattedDate

class StoryAdapter(
    private val onItemClick: (ListStoryItem, ItemStoryBinding) -> Unit
) : PagingDataAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        getItem(position)?.let { story -> holder.bind(story) }
    }

    inner class StoryViewHolder(
        private val binding: ItemStoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: ListStoryItem) {
            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description
            binding.tvItemDate.text = story.createdAt.toFormattedDate()
            Glide.with(itemView)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(binding.ivItemPhoto)
            itemView.setOnClickListener { onItemClick(story, binding) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean =
                oldItem == newItem
        }
    }
}
