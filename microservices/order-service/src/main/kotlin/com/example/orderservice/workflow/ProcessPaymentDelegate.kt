package com.example.orderservice.workflow

import com.example.orderservice.config.RabbitConfig
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class ProcessPaymentDelegate(
    private val rabbitTemplate: RabbitTemplate
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val orderId = UUID.fromString(execution.getVariable("orderId") as String)
        val userId = UUID.fromString(execution.getVariable("userId") as String)
        val amount = BigDecimal(execution.getVariable("amount").toString())
        val paymentMethod = execution.getVariable("paymentMethod") as String? ?: "CARD"

        val message = mapOf(
            "orderId" to orderId.toString(),
            "userId" to userId.toString(),
            "amount" to amount,
            "paymentMethod" to paymentMethod
        )
        rabbitTemplate.convertAndSend(
            RabbitConfig.PAYMENT_EXCHANGE,
            RabbitConfig.PAYMENT_PROCESS,
            message
        )
    }
}
