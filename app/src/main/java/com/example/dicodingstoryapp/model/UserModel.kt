package com.example.dicodingstoryapp.model

data class UserModel(
    val name: String,
    val email: String,
    val password: String,
    val isLogin: Boolean,
    val id: String,
    val token: String
)
