package com.example.dicodingstoryapp.ui.detail

import androidx.lifecycle.ViewModel
import com.example.dicodingstoryapp.model.ListStoryItem

class DetailViewModel : ViewModel() {
    lateinit var storyItem: ListStoryItem

    fun setDetailStory(story: ListStoryItem): ListStoryItem {
        storyItem = story
        return storyItem
    }
}