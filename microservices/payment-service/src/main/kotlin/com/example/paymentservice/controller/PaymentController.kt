package com.example.paymentservice.controller

import com.example.paymentservice.dto.PaymentDto
import com.example.paymentservice.dto.ProcessPaymentRequest
import com.example.paymentservice.dto.RefundRequest
import com.example.paymentservice.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment processing and management")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    @Operation(summary = "Process payment", description = "Process a new payment for an order")
    fun processPayment(@Valid @RequestBody request: ProcessPaymentRequest): ResponseEntity<PaymentDto> {
        val payment = paymentService.processPayment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(payment)
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by ID")
    fun getPaymentById(@PathVariable paymentId: UUID): ResponseEntity<PaymentDto> {
        val payment = paymentService.getPaymentById(paymentId)
        return if (payment != null) {
            ResponseEntity.ok(payment)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order", description = "Retrieve all payments for a specific order")
    fun getPaymentsByOrderId(@PathVariable orderId: UUID): ResponseEntity<List<PaymentDto>> {
        val payments = paymentService.getPaymentsByOrderId(orderId)
        return ResponseEntity.ok(payments)
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user", description = "Retrieve paginated payments for a specific user")
    fun getPaymentsByUserId(
        @PathVariable userId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<PaymentDto>> {
        val payments = paymentService.getPaymentsByUserId(userId, pageable)
        return ResponseEntity.ok(payments)
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund payment", description = "Process a refund for an existing payment")
    fun refundPayment(
        @PathVariable paymentId: UUID,
        @Valid @RequestBody refundRequest: RefundRequest
    ): ResponseEntity<PaymentDto> {
        return try {
            val payment = paymentService.refundPayment(paymentId, refundRequest)
            if (payment != null) {
                ResponseEntity.ok(payment)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }
}
