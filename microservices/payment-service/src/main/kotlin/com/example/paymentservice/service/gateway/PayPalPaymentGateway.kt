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
 * PayPal Payment Gateway Implementation
 * In real scenario, this would integrate with PayPal's REST API
 */
@Service
class PayPalPaymentGateway : PaymentGatewayService {
    private val logger = LoggerFactory.getLogger(PayPalPaymentGateway::class.java)

    override fun getGatewayType(): PaymentGateway = PaymentGateway.PAYPAL

    override fun processPayment(
        paymentId: UUID,
        amount: BigDecimal,
        currency: String,
        paymentDetails: PaymentDetails
    ): PaymentGatewayResponse {
        logger.info("Processing PayPal payment {} for amount {}", paymentId, amount)
        
        try {
            // Simulate external API call delay
            Thread.sleep(Random.nextLong(1500, 4000))
            
            // Simulate payment processing logic
            val success = Random.nextDouble() > 0.05 // 95% success rate
            
            return if (success) {
                PaymentGatewayResponse(
                    success = true,
                    transactionId = "paypal_${UUID.randomUUID()}",
                    status = "completed",
                    message = "PayPal payment completed successfully",
                    amount = amount,
                    currency = currency,
                    gatewaySpecificData = mapOf(
                        "payment_id" to "PAY-${Random.nextInt(10000000, 99999999)}",
                        "payer_id" to "PAYER${Random.nextInt(1000000, 9999999)}",
                        "transaction_fee" to (amount * BigDecimal("0.029") + BigDecimal("0.30"))
                    )
                )
            } else {
                PaymentGatewayResponse(
                    success = false,
                    transactionId = null,
                    status = "failed",
                    message = "PayPal payment failed - insufficient funds",
                    gatewaySpecificData = mapOf(
                        "error_name" to "INSUFFICIENT_FUNDS",
                        "error_message" to "The instrument presented was either declined by the processor or bank, or it cannot be used for this payment."
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error processing PayPal payment", e)
            return PaymentGatewayResponse(
                success = false,
                transactionId = null,
                status = "error",
                message = "PayPal gateway communication error: ${e.message}"
            )
        }
    }

    override fun refundPayment(
        gatewayTransactionId: String,
        refundRequest: RefundRequest
    ): PaymentGatewayResponse {
        logger.info("Processing PayPal refund for transaction {}", gatewayTransactionId)
        
        try {
            Thread.sleep(Random.nextLong(1000, 2000))
            
            return PaymentGatewayResponse(
                success = true,
                transactionId = "paypal_refund_${UUID.randomUUID()}",
                status = "completed",
                message = "PayPal refund processed successfully",
                amount = refundRequest.amount,
                gatewaySpecificData = mapOf(
                    "refund_id" to "REFUND-${Random.nextInt(10000000, 99999999)}",
                    "reason_code" to "refund",
                    "refund_reason" to refundRequest.reason
                )
            )
        } catch (e: Exception) {
            logger.error("Error processing PayPal refund", e)
            return PaymentGatewayResponse(
                success = false,
                transactionId = null,
                status = "error",
                message = "PayPal refund failed: ${e.message}"
            )
        }
    }

    override fun getPaymentStatus(gatewayTransactionId: String): PaymentGatewayResponse {
        return PaymentGatewayResponse(
            success = true,
            transactionId = gatewayTransactionId,
            status = "completed",
            message = "PayPal payment status retrieved"
        )
    }

    override fun isAvailable(): Boolean = true
}
