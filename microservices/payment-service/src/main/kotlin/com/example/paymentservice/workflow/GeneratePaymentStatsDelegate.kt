package com.example.paymentservice.workflow

import com.example.paymentservice.service.PaymentService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class GeneratePaymentStatsDelegate(
    private val paymentService: PaymentService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        paymentService.generatePaymentStatistics()
    }
}
