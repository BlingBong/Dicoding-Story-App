package com.example.dicodingstoryapp.ui.register

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dicodingstoryapp.data.remote.ApiConfig
import com.example.dicodingstoryapp.model.ApiResponse
import com.example.dicodingstoryapp.utils.ApiCallbackString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    fun register(name: String, email: String, password: String, callback: ApiCallbackString) {
        ApiConfig.getApiService()
            .register(name, email, password)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error!!)
                            callback.responseState(true, "success")
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

    companion object {
        private const val TAG = "RegisterViewModel"
    }
}