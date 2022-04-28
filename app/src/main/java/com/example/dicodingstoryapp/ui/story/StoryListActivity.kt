package com.example.dicodingstoryapp.ui.story

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.databinding.ActivityStoryListBinding
import com.example.dicodingstoryapp.ui.adapter.LoadingStateAdapter
import com.example.dicodingstoryapp.ui.adapter.StoryAdapter
import com.example.dicodingstoryapp.ui.addstory.AddStoryActivity
import com.example.dicodingstoryapp.ui.login.LoginActivity

class StoryListActivity : AppCompatActivity() {
    private lateinit var storyListBinding: ActivityStoryListBinding

    private val storyListViewModel: StoryListViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyListBinding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(storyListBinding.root)

        setupListener()
        setupOrientation()
        setupViewModel()
    }

    private fun setupViewModel() {
        storyListViewModel.getUser().observe(this) { it ->
            storyListBinding.pbList.visibility = View.VISIBLE
            if (!it.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                storyListBinding.pbList.visibility = View.GONE
                finish()
            } else {
                storyListViewModel.getAllStories(it.token).observe(this) {
                    val storyAdapter = StoryAdapter()
                    storyListBinding.pbList.visibility = View.GONE
                    storyListBinding.rvStory.adapter = storyAdapter.withLoadStateFooter(
                        footer = LoadingStateAdapter {
                            storyAdapter.retry()
                        }
                    )
                    storyAdapter.submitData(lifecycle, it)
                }
            }
        }
    }

    private fun setupListener() {
        storyListBinding.addStory.fabStory.setOnClickListener {
            startActivity(Intent(this@StoryListActivity, AddStoryActivity::class.java))
        }
    }

    private fun setupOrientation() {
        storyListBinding.apply {
            rvStory.setHasFixedSize(true)

            if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                rvStory.layoutManager = GridLayoutManager(this@StoryListActivity, 2)
            } else {
                rvStory.layoutManager = LinearLayoutManager(this@StoryListActivity)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_story_map -> {
                startActivity(Intent(this@StoryListActivity, StoryMapActivity::class.java))

                true
            }
            R.id.menu_translate -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))

                true
            }
            R.id.menu_logout -> {
                storyListViewModel.logout()
                AlertDialog.Builder(this).apply {
                    setTitle(getString(R.string.logout))
                    setMessage(getString(R.string.success_logout_message))
                    create()
                    show()
                }

                true
            }
            else -> false
        }
    }
}