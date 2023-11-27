package com.example.dicodingstoryapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.dicodingstoryapp.data.remote.ApiService
import com.example.dicodingstoryapp.model.ListStoryItem

class StoryPagingSource(private val apiService: ApiService, private val token: String?) :
    PagingSource<Int, ListStoryItem>() {
    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getAllStories("Bearer $token", position, params.loadSize)
            val responseList = responseData.listStory!!.toList()
            LoadResult.Page(
                data = responseList,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseList.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}