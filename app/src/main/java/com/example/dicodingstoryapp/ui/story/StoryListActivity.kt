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
import com.example.dicodingstoryapp.ui.adapter.StoryAdapter
import com.example.dicodingstoryapp.ui.addstory.AddStoryActivity
import com.example.dicodingstoryapp.ui.login.LoginActivity
import com.example.dicodingstoryapp.utils.ApiCallbackString

class StoryListActivity : AppCompatActivity() {
    private lateinit var storyListBinding: ActivityStoryListBinding
    private lateinit var storyAdapter: StoryAdapter
    private val storyViewModel: StoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyListBinding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(storyListBinding.root)

        setupListener()
        setupViewModel()
        setupOrientation()
    }

    private fun setupViewModel() {
        storyViewModel.getUser().observe(this) {
            if (!it.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                storyViewModel.getAllStories(it.token, object : ApiCallbackString {
                    override fun responseState(success: Boolean, message: String) {
                        showLoading(true)
                        if (!success) {
                            AlertDialog.Builder(this@StoryListActivity).apply {
                                setTitle(getString(R.string.failed))
                                setMessage(getString(R.string.fail_fetch))
                                setPositiveButton(getString(R.string.cont), null)
                                create()
                                show()
                            }
                            showLoading(false)
                        }
                    }
                })

                storyViewModel.itemStory.observe(this) { itemStory ->
                    storyAdapter.setList(itemStory)
                    showLoading(false)
                }
            }
        }
    }

    private fun setupListener() {
        storyAdapter = StoryAdapter(ArrayList())

        storyListBinding.addStory.fabStory.setOnClickListener {
            startActivity(Intent(this@StoryListActivity, AddStoryActivity::class.java))
        }
    }

    private fun setupOrientation() {
        storyListBinding.apply {
            rvStory.adapter = storyAdapter
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
                storyViewModel.logout()
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

    private fun showLoading(state: Boolean) {
        if (state) {
            storyListBinding.apply {
                tvChecker.visibility = View.GONE
                pbList.visibility = View.VISIBLE
            }
        } else {
            storyListBinding.apply {
                pbList.visibility = View.GONE
                rvStory.visibility = View.VISIBLE
            }
            if (storyAdapter.itemCount == 0) {
                storyListBinding.apply {
                    rvStory.visibility = View.GONE
                    tvChecker.visibility = View.VISIBLE
                }
            }
        }
    }
}