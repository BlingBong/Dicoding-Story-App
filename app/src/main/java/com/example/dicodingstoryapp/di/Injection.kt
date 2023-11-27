package com.example.dicodingstoryapp.di

import com.example.dicodingstoryapp.data.remote.ApiConfig
import com.example.dicodingstoryapp.data.repository.StoryRepository

object Injection {
    fun provideRepository(): StoryRepository {
        val apiService = ApiConfig.getApiService()
        return StoryRepository(apiService)
    }
}