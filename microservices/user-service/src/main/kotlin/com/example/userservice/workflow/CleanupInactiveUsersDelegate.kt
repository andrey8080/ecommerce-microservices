package com.example.userservice.workflow

import com.example.userservice.service.UserService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class CleanupInactiveUsersDelegate(
    private val userService: UserService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        userService.cleanupInactiveUsers()
    }
}
