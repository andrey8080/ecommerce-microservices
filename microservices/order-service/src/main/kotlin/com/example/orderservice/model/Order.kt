package com.example.orderservice.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    @Column(nullable = false)
    val userId: UUID,
    
    @Column(nullable = false)
    val orderNumber: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val shippingCost: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val finalAmount: BigDecimal,
    
    @Column(nullable = false)
    val shippingAddress: String,
    
    @Column(nullable = false)
    val billingAddress: String,
    
    @Column
    val paymentMethod: String? = null,
    
    @Column
    var paymentTransactionId: String? = null,
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val items: MutableList<OrderItem> = mutableListOf(),
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    var completedAt: LocalDateTime? = null
) {
    // JPA требует пустой конструктор
    constructor() : this(
        userId = UUID.randomUUID(),
        orderNumber = "",
        totalAmount = BigDecimal.ZERO,
        finalAmount = BigDecimal.ZERO,
        shippingAddress = "",
        billingAddress = ""
    )
}

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    
    @Column(nullable = false)
    val productId: UUID,
    
    @Column(nullable = false)
    val productName: String,
    
    @Column(nullable = false)
    val productSku: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val unitPrice: BigDecimal,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalPrice: BigDecimal
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    PAID,
    PAYMENT_FAILED
}
