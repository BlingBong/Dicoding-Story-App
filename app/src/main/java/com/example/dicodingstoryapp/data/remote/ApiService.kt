package com.example.dicodingstoryapp.data.remote

import com.example.dicodingstoryapp.model.AllStoriesResponse
import com.example.dicodingstoryapp.model.ApiResponse
import com.example.dicodingstoryapp.model.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Float,
        @Part("lon") lon: Float
    ): Call<ApiResponse>

    @GET("stories")
    fun getAllStories(
        @Header("Authorization") token: String
    ): Call<AllStoriesResponse>

    @GET("stories?location=1")
    fun getAllStoriesWithMap(
        @Header("Authorization") token: String
    ): Call<AllStoriesResponse>
}