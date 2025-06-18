package com.example.userservice.dto

import com.example.userservice.model.UserRole

data class StartUserProcessRequest(
    val username: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val role: UserRole = UserRole.USER
)
