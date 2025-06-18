package com.example.userservice.workflow

import com.example.userservice.dto.CreateUserRequest
import com.example.userservice.model.UserRole
import com.example.userservice.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class CreateUserDelegate(
    private val userService: UserService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val username = execution.getVariable("username") as String
        val email = execution.getVariable("email") as String
        val phoneNumber = execution.getVariable("phoneNumber") as String
        val password = execution.getVariable("password") as String
        val role = UserRole.valueOf(execution.getVariable("role") as String)
        val request = CreateUserRequest(
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            password = password,
            role = role
        )
        val user = userService.createUser(request)
        execution.setVariable("userId", user.id.toString())
    }
}
