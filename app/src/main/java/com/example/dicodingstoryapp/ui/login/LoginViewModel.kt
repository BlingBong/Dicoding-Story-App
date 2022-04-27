package com.example.dicodingstoryapp.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstoryapp.data.preference.UserPreferences
import com.example.dicodingstoryapp.data.remote.ApiConfig
import com.example.dicodingstoryapp.model.LoginResponse
import com.example.dicodingstoryapp.model.UserModel
import com.example.dicodingstoryapp.utils.ApiCallbackString
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences: UserPreferences = UserPreferences.getInstance(application)

    fun login(email: String, password: String, callback: ApiCallbackString) {
        ApiConfig.getApiService()
            .login(email, password)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error!!) {
                            callback.responseState(true, "success")
                            val model = UserModel(
                                responseBody.loginResult?.name.toString(),
                                email,
                                password,
                                true,
                                responseBody.loginResult?.userId.toString(),
                                responseBody.loginResult?.token.toString()
                            )
                            setUser(model)
                        }
                    } else {
                        Log.e(TAG, "Failure: ${response.message()}")
                        callback.responseState(false, response.message())
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e(TAG, "Failure: ${t.message}")
                    callback.responseState(false, t.message.toString())
                }
            })
    }

    fun setUser(user: UserModel) = viewModelScope.launch {
        userPreferences.setUser(user)
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}