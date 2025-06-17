package com.example.paymentservice.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val orderId: UUID,
    
    @Column(nullable = false)
    val userId: UUID,
    
    @Column(nullable = false, unique = true)
    val paymentReference: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "USD",
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.PENDING,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val method: PaymentMethod,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gateway: PaymentGateway,
    
    @Column
    val gatewayTransactionId: String? = null,
    
    @Column
    val gatewayResponse: String? = null,
    
    @Column
    val failureReason: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    val processedAt: LocalDateTime? = null,
    
    @Column
    val refundedAt: LocalDateTime? = null,
    
    @Column(precision = 10, scale = 2)
    val refundedAmount: BigDecimal? = null
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    CRYPTOCURRENCY,
    DIGITAL_WALLET
}

enum class PaymentGateway {
    STRIPE,
    PAYPAL,
    SQUARE,
    RAZORPAY,
    INTERNAL_MOCK
}
