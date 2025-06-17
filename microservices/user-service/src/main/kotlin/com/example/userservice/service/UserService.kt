package com.example.userservice.service

import com.example.userservice.config.RabbitConfig
import com.example.userservice.dto.CreateUserRequest
import com.example.userservice.dto.UserDto
import com.example.userservice.dto.UserEvent
import com.example.userservice.model.User
import com.example.userservice.model.UserRole
import com.example.userservice.repository.UserRepository
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()
) {

    fun createUser(request: CreateUserRequest): UserDto {
        // Проверяем, что пользователь с таким email или телефоном не существует
        userRepository.findByEmail(request.email)?.let {
            throw RuntimeException("User with email ${request.email} already exists")
        }
        
        userRepository.findByPhoneNumber(request.phoneNumber)?.let {
            throw RuntimeException("User with phone number ${request.phoneNumber} already exists")
        }
        
        userRepository.findByUsername(request.username)?.let {
            throw RuntimeException("User with username ${request.username} already exists")
        }
        
        val user = User(
            username = request.username,
            email = request.email,
            phoneNumber = request.phoneNumber,
            password = passwordEncoder.encode(request.password),
            role = request.role
        )
        
        val savedUser = userRepository.save(user)
        val userDto = savedUser.toDto()
        
        // Отправляем асинхронное сообщение о создании пользователя
        publishUserEvent("USER_CREATED", savedUser.id, userDto)
        
        return userDto
    }

    fun getUserById(id: UUID): UserDto? {
        return userRepository.findById(id).orElse(null)?.toDto()
    }

    fun getUserByEmail(email: String): UserDto? {
        return userRepository.findByEmail(email)?.toDto()
    }

    fun getUserByPhoneNumber(phoneNumber: String): UserDto? {
        return userRepository.findByPhoneNumber(phoneNumber)?.toDto()
    }

    fun updateUserRole(userId: UUID, role: UserRole): UserDto {
        val user = userRepository.findById(userId).orElseThrow { 
            RuntimeException("User not found with id: $userId") 
        }
        
        val updatedUser = user.copy(role = role)
        val savedUser = userRepository.save(updatedUser)
        val userDto = savedUser.toDto()
        
        // Отправляем асинхронное сообщение об обновлении пользователя
        publishUserEvent("USER_UPDATED", savedUser.id, userDto)
        
        return userDto
    }

    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
    }

    fun deleteUser(userId: UUID) {
        val user = userRepository.findById(userId).orElseThrow { 
            RuntimeException("User not found with id: $userId") 
        }
        
        userRepository.delete(user)
        
        // Отправляем асинхронное сообщение об удалении пользователя
        publishUserEvent("USER_DELETED", userId, user.toDto())
    }

    private fun publishUserEvent(eventType: String, userId: UUID, userDto: UserDto) {
        val event = UserEvent(
            eventType = eventType,
            userId = userId,
            userDto = userDto
        )
        
        val routingKey = when (eventType) {
            "USER_CREATED" -> "user.created"
            "USER_UPDATED" -> "user.updated"
            "USER_DELETED" -> "user.deleted"
            else -> "user.unknown"
        }
        
        rabbitTemplate.convertAndSend(RabbitConfig.USER_EXCHANGE, routingKey, event)
    }

    private fun User.toDto(): UserDto {
        return UserDto(
            id = this.id,
            username = this.username,
            email = this.email,
            phoneNumber = this.phoneNumber,
            role = this.role
        )
    }

    // Планировщик задач - очистка неактивных пользователей
    @Scheduled(cron = "0 0 2 * * ?") // Каждый день в 2:00
    fun cleanupInactiveUsers() {
        // Логика очистки неактивных пользователей
        // Например, удаление пользователей, которые не логинились больше года
        println("Running scheduled task: Cleanup inactive users")
        
        // Здесь можно добавить логику поиска неактивных пользователей
        // и отправку уведомлений через RabbitMQ
    }

    // Планировщик задач - отправка статистики пользователей
    @Scheduled(fixedRate = 3600000) // Каждый час
    fun sendUserStatistics() {
        val totalUsers = userRepository.count()
        println("Sending user statistics: Total users = $totalUsers")
        
        // Отправляем статистику через RabbitMQ
        val statisticsEvent = mapOf(
            "eventType" to "USER_STATISTICS",
            "totalUsers" to totalUsers,
            "timestamp" to System.currentTimeMillis()
        )
        
        rabbitTemplate.convertAndSend(RabbitConfig.USER_EXCHANGE, "user.statistics", statisticsEvent)
    }
}
