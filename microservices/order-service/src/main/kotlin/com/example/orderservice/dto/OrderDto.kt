package com.example.orderservice.dto

import com.example.orderservice.model.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class OrderDto(
    val id: UUID,
    val userId: UUID,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val shippingCost: BigDecimal,
    val taxAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val shippingAddress: String,
    val billingAddress: String,
    val paymentMethod: String?,
    val paymentTransactionId: String?,
    val items: List<OrderItemDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?
)

data class OrderItemDto(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val totalPrice: BigDecimal
)

data class CreateOrderRequest(
    val userId: UUID,
    val items: List<CreateOrderItemRequest>,
    val shippingAddress: String,
    val billingAddress: String,
    val paymentMethod: String
)

data class CreateOrderItemRequest(
    val productId: UUID,
    val productName: String,
    val productSku: String,
    val unitPrice: BigDecimal,
    val quantity: Int
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus,
    val paymentTransactionId: String? = null
)

data class OrderEvent(
    val eventType: String,
    val orderId: UUID,
    val userId: UUID,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val items: List<OrderItemEvent>,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class OrderItemEvent(
    val productId: UUID,
    val quantity: Int,
    val unitPrice: BigDecimal
)

data class StockReservationRequest(
    val orderId: UUID,
    val items: List<StockReservationItem>
)

data class StockReservationItem(
    val productId: UUID,
    val quantity: Int
)

data class StockReservationResponse(
    val orderId: UUID,
    val success: Boolean,
    val message: String,
    val failedItems: List<StockReservationItem> = emptyList()
)
