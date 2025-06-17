package com.example.paymentservice.service.gateway

import com.example.paymentservice.dto.PaymentDetails
import com.example.paymentservice.dto.RefundRequest
import com.example.paymentservice.model.PaymentGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

/**
 * Stripe Payment Gateway Implementation
 * In real scenario, this would integrate with Stripe's API using their SDK
 */
@Service
class StripePaymentGateway : PaymentGatewayService {
    private val logger = LoggerFactory.getLogger(StripePaymentGateway::class.java)

    override fun getGatewayType(): PaymentGateway = PaymentGateway.STRIPE

    override fun processPayment(
        paymentId: UUID,
        amount: BigDecimal,
        currency: String,
        paymentDetails: PaymentDetails
    ): PaymentGatewayResponse {
        logger.info("Processing Stripe payment {} for amount {}", paymentId, amount)
        
        try {
            // Simulate external API call delay
            Thread.sleep(Random.nextLong(1000, 3000))
            
            // Simulate payment processing logic
            val success = Random.nextDouble() > 0.1 // 90% success rate
            
            return if (success) {
                PaymentGatewayResponse(
                    success = true,
                    transactionId = "stripe_${UUID.randomUUID()}",
                    status = "succeeded",
                    message = "Payment processed successfully",
                    amount = amount,
                    currency = currency,
                    gatewaySpecificData = mapOf(
                        "charge_id" to "ch_${Random.nextInt(100000, 999999)}",
                        "receipt_url" to "https://stripe.com/receipts/fake_receipt"
                    )
                )
            } else {
                PaymentGatewayResponse(
                    success = false,
                    transactionId = null,
                    status = "failed",
                    message = "Card declined",
                    gatewaySpecificData = mapOf(
                        "decline_code" to "generic_decline",
                        "error_code" to "card_declined"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error processing Stripe payment", e)
            return PaymentGatewayResponse(
                success = false,
                transactionId = null,
                status = "error",
                message = "Gateway communication error: ${e.message}"
            )
        }
    }

    override fun refundPayment(
        gatewayTransactionId: String,
        refundRequest: RefundRequest
    ): PaymentGatewayResponse {
        logger.info("Processing Stripe refund for transaction {}", gatewayTransactionId)
        
        try {
            Thread.sleep(Random.nextLong(500, 1500))
            
            return PaymentGatewayResponse(
                success = true,
                transactionId = "stripe_refund_${UUID.randomUUID()}",
                status = "refunded",
                message = "Refund processed successfully",
                amount = refundRequest.amount,
                gatewaySpecificData = mapOf(
                    "refund_id" to "re_${Random.nextInt(100000, 999999)}",
                    "reason" to refundRequest.reason
                )
            )
        } catch (e: Exception) {
            logger.error("Error processing Stripe refund", e)
            return PaymentGatewayResponse(
                success = false,
                transactionId = null,
                status = "error",
                message = "Refund failed: ${e.message}"
            )
        }
    }

    override fun getPaymentStatus(gatewayTransactionId: String): PaymentGatewayResponse {
        return PaymentGatewayResponse(
            success = true,
            transactionId = gatewayTransactionId,
            status = "succeeded",
            message = "Payment status retrieved"
        )
    }

    override fun isAvailable(): Boolean = true
}
