package com.example.orderservice.workflow

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import com.example.orderservice.service.OrderService
import com.example.orderservice.dto.CreateOrderRequest
import java.util.UUID

@Component
class CreateOrderDelegate(
    private val orderService: OrderService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val userId = UUID.fromString(execution.getVariable("userId") as String)
        val request = CreateOrderRequest(
            userId = userId,
            items = emptyList(),
            shippingAddress = "",
            billingAddress = "",
            paymentMethod = "AUTO"
        )
        val order = orderService.createOrder(request)
        execution.setVariable("orderId", order.id.toString())
        execution.setVariable("amount", order.finalAmount)
        execution.setVariable("paymentMethod", order.paymentMethod)
    }
}
