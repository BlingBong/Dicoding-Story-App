package com.example.dicodingstoryapp.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ItemStoryBinding
import com.example.dicodingstoryapp.model.ListStoryItem
import com.example.dicodingstoryapp.ui.detail.DetailActivity
import com.example.dicodingstoryapp.utils.dateFormatter

class StoryAdapter(private val listStory: ArrayList<ListStoryItem>) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setList(stories: ArrayList<ListStoryItem>) {
        listStory.apply {
            clear()
            addAll(stories)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val itemStoryBinding =
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(itemStoryBinding, parent.context)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(listStory[position])
    }

    override fun getItemCount(): Int = listStory.size

    inner class StoryViewHolder(
        private var itemStoryBinding: ItemStoryBinding,
        private var context: Context
    ) : RecyclerView.ViewHolder(itemStoryBinding.root) {
        fun bind(stories: ListStoryItem) {
            Glide.with(itemView.context)
                .load(stories.photoUrl)
                .into(itemStoryBinding.ivPicture)

            itemStoryBinding.apply {
                tvName.text = stories.name
                tvDesc.text = stories.description
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    tvCreated.text = context.getString(
                        R.string.created_at,
                        dateFormatter(stories.createdAt.toString())
                    )
                else
                    tvCreated.text = context.getString(R.string.created_at, stories.createdAt)
            }

            itemView.setOnClickListener {
                val data: ListStoryItem = listStory[adapterPosition]
                val moveToDetail = Intent(itemView.context, DetailActivity::class.java)
                moveToDetail.putExtra(DetailActivity.EXTRA_STORY, data)

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
}