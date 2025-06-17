package com.example.userservice.dto

import java.util.*

data class UserEvent(
    val eventType: String,
    val userId: UUID,
    val userDto: UserDto,
    val timestamp: Long = System.currentTimeMillis()
)
