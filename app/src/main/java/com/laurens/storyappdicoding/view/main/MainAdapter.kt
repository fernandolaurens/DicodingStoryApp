package com.laurens.storyappdicoding.view.main

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.laurens.storyappdicoding.data.pref.ListStoryItem
import com.laurens.storyappdicoding.databinding.ItemviewStoryBinding
import com.laurens.storyappdicoding.view.Cerita.CeritaDetailActivity
import com.laurens.storyappdicoding.view.Cerita.CeritaDetailActivity.Companion.EXTRA_STORY

class MainAdapter : PagingDataAdapter<ListStoryItem, MainAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemviewStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    class MyViewHolder(private val binding: ItemviewStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.tvStoryName.text = story.name
            binding.tvStoryDesc.text = story.description
            Log.d("mainadapter", "photourl: ${story.photoUrl}")
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(binding.ivPhoto)
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, CeritaDetailActivity::class.java)
                intent.putExtra(EXTRA_STORY,story as Parcelable)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.tvStoryName, "tv_story_name"),
                        Pair(binding.tvStoryDesc, "tv_story_desc"),
                        Pair(binding.ivPhoto, "iv_photo"),
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}