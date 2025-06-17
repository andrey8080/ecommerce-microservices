package com.example.userservice.repository

import com.example.userservice.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByPhoneNumber(phoneNumber: String): User?
    fun findByUsername(username: String): User?
    fun findByEmailOrPhoneNumber(email: String, phoneNumber: String): User?
}
