package com.example.cartservice.workflow

import com.example.cartservice.service.CartService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class ClearCartDelegate(
    private val cartService: CartService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val userId = execution.getVariable("userId") as String
        cartService.clearCart(userId)
    }
}
