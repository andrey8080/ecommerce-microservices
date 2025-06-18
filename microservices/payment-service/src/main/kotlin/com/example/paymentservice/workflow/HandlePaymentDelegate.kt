package com.example.paymentservice.workflow

import com.example.paymentservice.dto.PaymentDetails
import com.example.paymentservice.model.PaymentMethod
import com.example.paymentservice.dto.ProcessPaymentRequest
import com.example.paymentservice.service.PaymentService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class HandlePaymentDelegate(
    private val paymentService: PaymentService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val orderId = UUID.fromString(execution.getVariable("orderId") as String)
        val userId = UUID.fromString(execution.getVariable("userId") as String)
        val amount = BigDecimal(execution.getVariable("amount").toString())
        val method = PaymentMethod.valueOf(execution.getVariable("paymentMethod") as String)

        val request = ProcessPaymentRequest(
            orderId = orderId,
            userId = userId,
            amount = amount,
            paymentMethod = method,
            paymentDetails = PaymentDetails()
        )
        paymentService.processPayment(request)
    }
}
