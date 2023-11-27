package com.example.dicodingstoryapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.dicodingstoryapp.data.paging.StoryPagingSource
import com.example.dicodingstoryapp.data.remote.ApiService
import com.example.dicodingstoryapp.model.ListStoryItem

class StoryRepository(private val apiService: ApiService) {
    fun getAllStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                initialLoadSize = 3 // the default one (9) leaves a bug where the 10th-16th data duplicate 3th-9th
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, token)
            }
        ).liveData
    }
}