package com.example.paymentservice.dto

import com.example.paymentservice.model.PaymentStatus
import com.example.paymentservice.model.PaymentMethod
import com.example.paymentservice.model.PaymentGateway
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PaymentDto(
    val id: UUID,
    val orderId: UUID,
    val userId: UUID,
    val paymentReference: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val method: PaymentMethod,
    val gateway: PaymentGateway,
    val gatewayTransactionId: String?,
    val failureReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val processedAt: LocalDateTime?,
    val refundedAt: LocalDateTime?,
    val refundedAmount: BigDecimal?
)

data class ProcessPaymentRequest(
    val orderId: UUID,
    val userId: UUID,
    val amount: BigDecimal,
    val currency: String = "USD",
    val paymentMethod: PaymentMethod,
    val paymentDetails: PaymentDetails
)

data class PaymentDetails(
    val cardNumber: String? = null,
    val cardHolderName: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null,
    val cvv: String? = null,
    val paypalEmail: String? = null,
    val bankAccount: String? = null,
    val routingNumber: String? = null
)

data class RefundRequest(
    val amount: BigDecimal,
    val reason: String
)

data class PaymentEvent(
    val eventType: String,
    val paymentId: UUID,
    val orderId: UUID,
    val userId: UUID,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val paymentReference: String,
    val gatewayTransactionId: String? = null,
    val failureReason: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class PaymentWebhookEvent(
    val gateway: PaymentGateway,
    val eventType: String,
    val transactionId: String,
    val status: String,
    val amount: BigDecimal?,
    val currency: String?,
    val timestamp: LocalDateTime,
    val rawData: Map<String, Any>
)

data class PaymentStatistics(
    val totalPayments: Long,
    val successfulPayments: Long,
    val failedPayments: Long,
    val totalRevenue: BigDecimal,
    val averagePaymentAmount: BigDecimal,
    val paymentsByMethod: Map<PaymentMethod, Long>,
    val paymentsByGateway: Map<PaymentGateway, Long>,
    val period: String
)
