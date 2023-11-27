package com.example.dicodingstoryapp.ui.story

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.dicodingstoryapp.data.preference.UserPreferences
import com.example.dicodingstoryapp.data.remote.ApiConfig
import com.example.dicodingstoryapp.model.AllStoriesResponse
import com.example.dicodingstoryapp.model.ListStoryItem
import com.example.dicodingstoryapp.utils.ApiCallbackString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryMapViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences: UserPreferences = UserPreferences.getInstance(application)
    val itemStory = MutableLiveData<ArrayList<ListStoryItem>>()

    fun getAllStoriesWithMap(token: String, callback: ApiCallbackString) {
        ApiConfig.getApiService()
            .getAllStoriesWithMap("Bearer $token")
            .enqueue(object : Callback<AllStoriesResponse> {
                override fun onResponse(
                    call: Call<AllStoriesResponse>,
                    response: Response<AllStoriesResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error!!) {
                            callback.responseState(true, "success")
                            itemStory.value = responseBody.listStory!!
                        }
                    } else {
                        Log.e(TAG, "Failure: ${response.message()}")
                        callback.responseState(false, response.message())
                    }
                }

                override fun onFailure(call: Call<AllStoriesResponse>, t: Throwable) {
                    Log.e(TAG, "Failure: ${t.message}")
                    callback.responseState(false, t.message.toString())
                }
            })
    }

    fun getUser() = userPreferences.getUser().asLiveData()

    companion object {
        private const val TAG = "StoryMapViewModel"
    }
}