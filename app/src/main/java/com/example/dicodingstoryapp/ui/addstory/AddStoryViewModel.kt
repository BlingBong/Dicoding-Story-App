package com.example.dicodingstoryapp.ui.addstory

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.dicodingstoryapp.data.preference.UserPreferences
import com.example.dicodingstoryapp.data.remote.ApiConfig
import com.example.dicodingstoryapp.model.ApiResponse
import com.example.dicodingstoryapp.utils.ApiCallbackString
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences: UserPreferences = UserPreferences.getInstance(application)

    fun addStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: Float,
        lon: Float,
        callback: ApiCallbackString
    ) {
        ApiConfig.getApiService()
            .addStory("Bearer $token", imageMultipart, description, lat, lon)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error!!) {
                            callback.responseState(true, "success")
                        }
                    } else {
                        Log.e(TAG, "Failure: ${response.message()}")
                        callback.responseState(false, response.message())
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e(TAG, "Failure: ${t.message}")
                    callback.responseState(false, t.message.toString())
                }
            })
    }

    fun getUser() = userPreferences.getUser().asLiveData()

    companion object {
        private const val TAG = "AddStoryViewModel"
    }
}