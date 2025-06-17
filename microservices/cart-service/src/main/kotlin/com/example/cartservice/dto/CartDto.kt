package com.example.cartservice.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class CartDto(
    val id: String,
    val userId: String,
    val items: List<CartItemDto>,
    val totalPrice: BigDecimal,
    val totalItems: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CartItemDto(
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int
)

data class AddToCartRequest(
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)

data class CartEvent(
    val eventType: String,
    val userId: String,
    val cartId: String,
    val productId: String? = null,
    val quantity: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
