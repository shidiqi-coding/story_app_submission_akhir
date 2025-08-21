package com.dicoding.storyapp.view.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.database.StoryEntity
import com.dicoding.storyapp.databinding.ItemListStoryBinding

class ListStoryAdapter(
     private val onItemClicked: (StoryEntity, ImageView, TextView, TextView) -> Unit
) : PagingDataAdapter<StoryEntity, ListStoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
          val binding = ItemListStoryBinding.inflate(
               LayoutInflater.from(parent.context), parent, false
          )
          return StoryViewHolder(binding)
     }

     @SuppressLint("SetTextI18n")
     override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
          val story = getItem(position)
          if (story != null) {
               holder.bind(story)

               // Shared element transition name
               holder.binding.imgStory.transitionName = "image_${story.id}"
               holder.binding.tvNameStory.transitionName = "name_${story.id}"
               holder.binding.tvSummaryStory.transitionName = "description_${story.id}"

               holder.itemView.setOnClickListener {
                    onItemClicked(
                         story,
                         holder.binding.imgStory,
                         holder.binding.tvNameStory,
                         holder.binding.tvSummaryStory
                    )
               }
          }
     }

     class StoryViewHolder(val binding: ItemListStoryBinding) :
          RecyclerView.ViewHolder(binding.root) {

          fun bind(story: StoryEntity) {
               binding.tvNameStory.text = story.name
               binding.tvSummaryStory.text = story.description
               Glide.with(binding.root.context)
                    .load(story.photoUrl)
                    .into(binding.imgStory)
          }
     }

     class StoryDiffCallback : DiffUtil.ItemCallback<StoryEntity>() {
          override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
               return oldItem.id == newItem.id
          }

          override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
               return oldItem == newItem
          }
     }
}
