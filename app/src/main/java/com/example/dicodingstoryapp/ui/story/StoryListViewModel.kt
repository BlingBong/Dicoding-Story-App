package com.example.dicodingstoryapp.ui.story

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dicodingstoryapp.data.preference.UserPreferences
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.di.Injection
import com.example.dicodingstoryapp.model.ListStoryItem
import kotlinx.coroutines.launch

class StoryListViewModel(context: Context, private val storyRepository: StoryRepository) :
    ViewModel() {
    private val userPreferences: UserPreferences = UserPreferences.getInstance(context)

    fun getAllStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return storyRepository.getAllStories(token).cachedIn(viewModelScope)
    }

    fun getUser() = userPreferences.getUser().asLiveData()

    fun logout() = viewModelScope.launch {
        userPreferences.logout()
    }
}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryListViewModel(context, Injection.provideRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}