package com.example.paymentservice.service.gateway

import com.example.paymentservice.dto.PaymentDetails
import com.example.paymentservice.dto.RefundRequest
import com.example.paymentservice.model.PaymentGateway
import java.math.BigDecimal
import java.util.*

/**
 * Payment Gateway Interface - JCA-style integration point
 * This represents the contract for external payment system integration
 */
interface PaymentGatewayService {
    
    fun getGatewayType(): PaymentGateway
    
    fun processPayment(
        paymentId: UUID,
        amount: BigDecimal,
        currency: String,
        paymentDetails: PaymentDetails
    ): PaymentGatewayResponse
    
    fun refundPayment(
        gatewayTransactionId: String,
        refundRequest: RefundRequest
    ): PaymentGatewayResponse
    
    fun getPaymentStatus(gatewayTransactionId: String): PaymentGatewayResponse
    
    fun isAvailable(): Boolean
}

data class PaymentGatewayResponse(
    val success: Boolean,
    val transactionId: String?,
    val status: String,
    val message: String,
    val amount: BigDecimal? = null,
    val currency: String? = null,
    val gatewaySpecificData: Map<String, Any> = emptyMap()
)
