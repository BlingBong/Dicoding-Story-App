package com.example.dicodingstoryapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ItemStoryBinding
import com.example.dicodingstoryapp.model.ListStoryItem
import com.example.dicodingstoryapp.ui.detail.DetailActivity
import com.example.dicodingstoryapp.utils.dateFormatter

class StoryAdapter :
    PagingDataAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val itemStoryBinding =
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(itemStoryBinding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val data = getItem(position)

        if (data != null) {
            holder.bind(data)
        }
    }

    inner class StoryViewHolder(private val itemStoryBinding: ItemStoryBinding) :
        RecyclerView.ViewHolder(itemStoryBinding.root) {
        fun bind(stories: ListStoryItem) {

            Glide.with(itemView)
                .load(stories.photoUrl)
                .into(itemStoryBinding.ivPicture)

            itemStoryBinding.apply {
                tvName.text = stories.name
                tvDesc.text = stories.description
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    tvCreated.text = itemView.context.getString(
                        R.string.created_at,
                        dateFormatter(stories.createdAt.toString())
                    )
                else
                    tvCreated.text =
                        itemView.context.getString(R.string.created_at, stories.createdAt)
            }

            itemView.setOnClickListener {
                val moveToDetail = Intent(itemView.context, DetailActivity::class.java)
                moveToDetail.putExtra(DetailActivity.EXTRA_STORY, stories)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(itemStoryBinding.ivPicture, "picture"),
                        Pair(itemStoryBinding.tvName, "name"),
                        Pair(itemStoryBinding.tvDesc, "desc"),
                        Pair(itemStoryBinding.tvCreated, "date")
                    )
                itemView.context.startActivity(moveToDetail, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}