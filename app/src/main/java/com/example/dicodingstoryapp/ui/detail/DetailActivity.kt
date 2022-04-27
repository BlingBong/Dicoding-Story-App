package com.example.dicodingstoryapp.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ActivityDetailBinding
import com.example.dicodingstoryapp.model.ListStoryItem
import com.example.dicodingstoryapp.utils.dateFormatter

class DetailActivity : AppCompatActivity() {
    private lateinit var detailBinding: ActivityDetailBinding
    private lateinit var storyItem: ListStoryItem
    private val detailViewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)

        setData()
    }

    private fun setData() {
        storyItem = intent.getParcelableExtra(EXTRA_STORY)!!
        detailViewModel.setDetailStory(storyItem)

        detailBinding.apply {
            Glide.with(this@DetailActivity)
                .load(detailViewModel.storyItem.photoUrl)
                .into(ivPicture)

            tvName.text = detailViewModel.storyItem.name
            tvDesc.text = detailViewModel.storyItem.description
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                tvCreated.text = getString(
                    R.string.created_at,
                    dateFormatter(detailViewModel.storyItem.createdAt.toString())
                )
            else
                tvCreated.text = getString(R.string.created_at, detailViewModel.storyItem.createdAt)
        }

        val actionBar = supportActionBar
        if (actionBar != null) {
            title = getString(R.string.story_detail, detailViewModel.storyItem.name)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_home)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val logoutMenu = menu.findItem(R.id.menu_logout)
        logoutMenu.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_translate -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))

                true
            }
            else -> false
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}