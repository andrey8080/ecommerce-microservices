package com.example.paymentservice.service

import com.example.paymentservice.config.RabbitConfig
import com.example.paymentservice.dto.*
import com.example.paymentservice.model.Payment
import com.example.paymentservice.model.PaymentStatus
import com.example.paymentservice.model.PaymentGateway
import com.example.paymentservice.model.PaymentMethod
import com.example.paymentservice.repository.PaymentRepository
import com.example.paymentservice.service.gateway.PaymentGatewayService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val paymentGateways: List<PaymentGatewayService>
) {
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    @RabbitListener(queues = [RabbitConfig.PAYMENT_PROCESSING_QUEUE])
    fun handlePaymentProcessing(paymentRequest: Map<String, Any>) {
        logger.info("Received payment processing request")
        
        try {
            val orderId = UUID.fromString(paymentRequest["orderId"] as String)
            val userId = UUID.fromString(paymentRequest["userId"] as String)
            val amount = BigDecimal(paymentRequest["amount"].toString())
            val paymentMethod = paymentRequest["paymentMethod"] as String
            
            // Create payment record
            val payment = Payment(
                orderId = orderId,
                userId = userId,
                paymentReference = generatePaymentReference(),
                amount = amount,
                method = PaymentMethod.valueOf(paymentMethod),
                gateway = selectPaymentGateway(PaymentMethod.valueOf(paymentMethod)),
                status = PaymentStatus.PENDING
            )
            
            val savedPayment = paymentRepository.save(payment)
            
            // Process payment asynchronously
            processPaymentAsync(savedPayment.id, createMockPaymentDetails())
            
        } catch (e: Exception) {
            logger.error("Error processing payment request", e)
        }
    }

    @Async
    fun processPaymentAsync(paymentId: UUID, paymentDetails: PaymentDetails): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            processPaymentInternal(paymentId, paymentDetails)
        }
    }

    fun processPayment(request: ProcessPaymentRequest): PaymentDto {
        logger.info("Processing payment for order {}", request.orderId)
        
        val payment = Payment(
            orderId = request.orderId,
            userId = request.userId,
            paymentReference = generatePaymentReference(),
            amount = request.amount,
            currency = request.currency,
            method = request.paymentMethod,
            gateway = selectPaymentGateway(request.paymentMethod),
            status = PaymentStatus.PROCESSING
        )
        
        val savedPayment = paymentRepository.save(payment)
        
        try {
            val gateway = getPaymentGateway(savedPayment.gateway)
            val response = gateway.processPayment(
                savedPayment.id,
                savedPayment.amount,
                savedPayment.currency,
                request.paymentDetails
            )
            
            val updatedPayment = if (response.success) {
                savedPayment.copy(
                    status = PaymentStatus.COMPLETED,
                    gatewayTransactionId = response.transactionId,
                    gatewayResponse = response.message,
                    processedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            } else {
                savedPayment.copy(
                    status = PaymentStatus.FAILED,
                    failureReason = response.message,
                    gatewayResponse = response.message,
                    updatedAt = LocalDateTime.now()
                )
            }
            
            val finalPayment = paymentRepository.save(updatedPayment)
            
            // Publish payment event
            publishPaymentEvent(finalPayment, if (response.success) RabbitConfig.PAYMENT_COMPLETED else RabbitConfig.PAYMENT_FAILED)
            
            // Update order status
            updateOrderPaymentStatus(finalPayment)
            
            logger.info("Payment {} processed with status {}", finalPayment.paymentReference, finalPayment.status)
            return convertToDto(finalPayment)
            
        } catch (e: Exception) {
            logger.error("Error processing payment {}", savedPayment.paymentReference, e)
            
            val failedPayment = savedPayment.copy(
                status = PaymentStatus.FAILED,
                failureReason = "Processing error: ${e.message}",
                updatedAt = LocalDateTime.now()
            )
            
            val finalPayment = paymentRepository.save(failedPayment)
            publishPaymentEvent(finalPayment, RabbitConfig.PAYMENT_FAILED)
            updateOrderPaymentStatus(finalPayment)
            
            return convertToDto(finalPayment)
        }
    }

    private fun processPaymentInternal(paymentId: UUID, paymentDetails: PaymentDetails) {
        val payment = paymentRepository.findById(paymentId).orElse(null) ?: return
        
        try {
            val updatedPayment = payment.copy(
                status = PaymentStatus.PROCESSING,
                updatedAt = LocalDateTime.now()
            )
            paymentRepository.save(updatedPayment)
            
            val gateway = getPaymentGateway(payment.gateway)
            val response = gateway.processPayment(
                payment.id,
                payment.amount,
                payment.currency,
                paymentDetails
            )
            
            val finalPayment = if (response.success) {
                payment.copy(
                    status = PaymentStatus.COMPLETED,
                    gatewayTransactionId = response.transactionId,
                    gatewayResponse = response.message,
                    processedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            } else {
                payment.copy(
                    status = PaymentStatus.FAILED,
                    failureReason = response.message,
                    gatewayResponse = response.message,
                    updatedAt = LocalDateTime.now()
                )
            }
            
            paymentRepository.save(finalPayment)
            
            // Publish events
            publishPaymentEvent(finalPayment, if (response.success) RabbitConfig.PAYMENT_COMPLETED else RabbitConfig.PAYMENT_FAILED)
            updateOrderPaymentStatus(finalPayment)
            
        } catch (e: Exception) {
            logger.error("Error in async payment processing", e)
            
            val failedPayment = payment.copy(
                status = PaymentStatus.FAILED,
                failureReason = "Async processing error: ${e.message}",
                updatedAt = LocalDateTime.now()
            )
            
            paymentRepository.save(failedPayment)
            publishPaymentEvent(failedPayment, RabbitConfig.PAYMENT_FAILED)
            updateOrderPaymentStatus(failedPayment)
        }
    }

    fun refundPayment(paymentId: UUID, refundRequest: RefundRequest): PaymentDto? {
        val payment = paymentRepository.findById(paymentId).orElse(null) ?: return null
        
        if (payment.status != PaymentStatus.COMPLETED) {
            throw IllegalStateException("Cannot refund payment with status ${payment.status}")
        }
        
        if (payment.gatewayTransactionId == null) {
            throw IllegalStateException("Cannot refund payment without gateway transaction ID")
        }
        
        try {
            val gateway = getPaymentGateway(payment.gateway)
            val response = gateway.refundPayment(payment.gatewayTransactionId, refundRequest)
            
            val refundedAmount = payment.refundedAmount?.plus(refundRequest.amount) ?: refundRequest.amount
            val newStatus = if (refundedAmount >= payment.amount) PaymentStatus.REFUNDED else PaymentStatus.PARTIALLY_REFUNDED
            
            val updatedPayment = payment.copy(
                status = newStatus,
                refundedAmount = refundedAmount,
                refundedAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            val finalPayment = paymentRepository.save(updatedPayment)
            publishPaymentEvent(finalPayment, RabbitConfig.PAYMENT_REFUNDED)
            
            logger.info("Refund processed for payment {}, amount: {}", payment.paymentReference, refundRequest.amount)
            return convertToDto(finalPayment)
            
        } catch (e: Exception) {
            logger.error("Error processing refund for payment {}", payment.paymentReference, e)
            throw e
        }
    }

    fun getPaymentById(paymentId: UUID): PaymentDto? {
        val payment = paymentRepository.findById(paymentId).orElse(null) ?: return null
        return convertToDto(payment)
    }

    fun getPaymentsByOrderId(orderId: UUID): List<PaymentDto> {
        return paymentRepository.findByOrderId(orderId).map { convertToDto(it) }
    }

    fun getPaymentsByUserId(userId: UUID, pageable: Pageable): Page<PaymentDto> {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { convertToDto(it) }
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    fun processStuckPayments() {
        logger.info("Processing stuck payments")
        
        try {
            val cutoffTime = LocalDateTime.now().minusMinutes(15)
            val stuckPayments = paymentRepository.findStuckPayments(cutoffTime)
            
            stuckPayments.forEach { payment ->
                try {
                    val gateway = getPaymentGateway(payment.gateway)
                    if (payment.gatewayTransactionId != null) {
                        val statusResponse = gateway.getPaymentStatus(payment.gatewayTransactionId)
                        
                        val updatedStatus = when (statusResponse.status.lowercase()) {
                            "succeeded", "completed" -> PaymentStatus.COMPLETED
                            "failed", "declined" -> PaymentStatus.FAILED
                            else -> PaymentStatus.FAILED
                        }
                        
                        val updatedPayment = payment.copy(
                            status = updatedStatus,
                            updatedAt = LocalDateTime.now(),
                            processedAt = if (updatedStatus == PaymentStatus.COMPLETED) LocalDateTime.now() else payment.processedAt
                        )
                        
                        paymentRepository.save(updatedPayment)
                        publishPaymentEvent(updatedPayment, if (updatedStatus == PaymentStatus.COMPLETED) RabbitConfig.PAYMENT_COMPLETED else RabbitConfig.PAYMENT_FAILED)
                        updateOrderPaymentStatus(updatedPayment)
                        
                        logger.info("Updated stuck payment {} to status {}", payment.paymentReference, updatedStatus)
                    } else {
                        // No gateway transaction ID, mark as failed
                        val failedPayment = payment.copy(
                            status = PaymentStatus.FAILED,
                            failureReason = "Payment stuck without gateway transaction ID",
                            updatedAt = LocalDateTime.now()
                        )
                        
                        paymentRepository.save(failedPayment)
                        publishPaymentEvent(failedPayment, RabbitConfig.PAYMENT_FAILED)
                        updateOrderPaymentStatus(failedPayment)
                    }
                } catch (e: Exception) {
                    logger.error("Error processing stuck payment {}", payment.paymentReference, e)
                }
            }
            
            if (stuckPayments.isNotEmpty()) {
                logger.info("Processed {} stuck payments", stuckPayments.size)
            }
        } catch (e: Exception) {
            logger.error("Error during stuck payment processing", e)
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    fun generatePaymentStatistics() {
        logger.info("Generating payment statistics")
        
        try {
            val now = LocalDateTime.now()
            val dayStart = now.toLocalDate().atStartOfDay()
            
            val dailyPayments = paymentRepository.findPaymentsByDateRange(dayStart, now)
            val stats = PaymentStatistics(
                totalPayments = dailyPayments.size.toLong(),
                successfulPayments = dailyPayments.count { it.status == PaymentStatus.COMPLETED }.toLong(),
                failedPayments = dailyPayments.count { it.status == PaymentStatus.FAILED }.toLong(),
                totalRevenue = dailyPayments.filter { it.status == PaymentStatus.COMPLETED }
                    .sumOf { it.amount },
                averagePaymentAmount = if (dailyPayments.isNotEmpty()) {
                    dailyPayments.sumOf { it.amount }.divide(BigDecimal(dailyPayments.size))
                } else BigDecimal.ZERO,
                paymentsByMethod = dailyPayments.groupBy { it.method }.mapValues { it.value.size.toLong() },
                paymentsByGateway = dailyPayments.groupBy { it.gateway }.mapValues { it.value.size.toLong() },
                period = "daily"
            )
            
            logger.info("Payment Statistics - {}", stats)
        } catch (e: Exception) {
            logger.error("Error generating payment statistics", e)
        }
    }

    private fun selectPaymentGateway(paymentMethod: PaymentMethod): PaymentGateway {
        return when (paymentMethod) {
            PaymentMethod.CREDIT_CARD, PaymentMethod.DEBIT_CARD -> PaymentGateway.STRIPE
            PaymentMethod.PAYPAL -> PaymentGateway.PAYPAL
            else -> PaymentGateway.STRIPE
        }
    }

    private fun getPaymentGateway(gatewayType: PaymentGateway): PaymentGatewayService {
        return paymentGateways.find { it.getGatewayType() == gatewayType }
            ?: throw IllegalArgumentException("Payment gateway $gatewayType not supported")
    }

    private fun publishPaymentEvent(payment: Payment, eventType: String) {
        try {
            val event = PaymentEvent(
                eventType = eventType,
                paymentId = payment.id,
                orderId = payment.orderId,
                userId = payment.userId,
                amount = payment.amount,
                status = payment.status,
                paymentReference = payment.paymentReference,
                gatewayTransactionId = payment.gatewayTransactionId,
                failureReason = payment.failureReason
            )
            
            rabbitTemplate.convertAndSend(
                RabbitConfig.PAYMENT_EXCHANGE,
                eventType,
                event
            )
            
            // Send notification
            sendPaymentNotification(payment, eventType)
            
        } catch (e: Exception) {
            logger.error("Failed to publish payment event", e)
        }
    }

    private fun updateOrderPaymentStatus(payment: Payment) {
        try {
            val orderUpdate = mapOf(
                "orderId" to payment.orderId,
                "paymentStatus" to payment.status,
                "paymentTransactionId" to payment.gatewayTransactionId,
                "amount" to payment.amount
            )
            
            rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                RabbitConfig.ORDER_PAYMENT_UPDATE,
                orderUpdate
            )
        } catch (e: Exception) {
            logger.error("Failed to update order payment status", e)
        }
    }

    private fun sendPaymentNotification(payment: Payment, eventType: String) {
        try {
            val notification = mapOf(
                "userId" to payment.userId,
                "paymentId" to payment.id,
                "orderId" to payment.orderId,
                "amount" to payment.amount,
                "status" to payment.status,
                "paymentReference" to payment.paymentReference,
                "eventType" to eventType,
                "message" to when (eventType) {
                    RabbitConfig.PAYMENT_COMPLETED -> "Payment of $${payment.amount} completed successfully"
                    RabbitConfig.PAYMENT_FAILED -> "Payment of $${payment.amount} failed"
                    RabbitConfig.PAYMENT_REFUNDED -> "Refund of $${payment.refundedAmount} processed"
                    else -> "Payment status updated"
                }
            )
            
            rabbitTemplate.convertAndSend(
                RabbitConfig.NOTIFICATION_EXCHANGE,
                RabbitConfig.NOTIFICATION_PAYMENT,
                notification
            )
        } catch (e: Exception) {
            logger.error("Failed to send payment notification", e)
        }
    }

    private fun generatePaymentReference(): String {
        return "PAY-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }

    private fun createMockPaymentDetails(): PaymentDetails {
        return PaymentDetails(
            cardNumber = "4111111111111111",
            cardHolderName = "John Doe",
            expiryMonth = 12,
            expiryYear = 2025,
            cvv = "123"
        )
    }

    private fun convertToDto(payment: Payment): PaymentDto {
        return PaymentDto(
            id = payment.id,
            orderId = payment.orderId,
            userId = payment.userId,
            paymentReference = payment.paymentReference,
            amount = payment.amount,
            currency = payment.currency,
            status = payment.status,
            method = payment.method,
            gateway = payment.gateway,
            gatewayTransactionId = payment.gatewayTransactionId,
            failureReason = payment.failureReason,
            createdAt = payment.createdAt,
            updatedAt = payment.updatedAt,
            processedAt = payment.processedAt,
            refundedAt = payment.refundedAt,
            refundedAmount = payment.refundedAmount
        )
    }
}
