package com.example.userservice.dto

import com.example.userservice.model.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.util.*

data class UserDto(
    val id: UUID? = null,
    @field:NotBlank(message = "Username is required")
    val username: String,
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    @field:NotBlank(message = "Phone number is required")
    val phoneNumber: String,
    val role: UserRole = UserRole.USER
)

data class CreateUserRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    @field:NotBlank(message = "Phone number is required")
    val phoneNumber: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
    val role: UserRole = UserRole.USER
)
