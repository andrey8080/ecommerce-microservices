package com.example.orderservice.service

import com.example.orderservice.config.RabbitConfig
import com.example.orderservice.dto.*
import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderItem
import com.example.orderservice.model.OrderStatus
import com.example.orderservice.repository.OrderRepository
import com.example.orderservice.repository.OrderItemRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Async
    private fun publishOrderEvent(order: Order, eventType: String) {
        try {
            val orderEvent = OrderEvent(
                eventType = eventType,
                orderId = order.id!!,
                orderNumber = order.orderNumber,
                userId = order.userId,
                status = order.status,
                totalAmount = order.totalAmount,
                items = emptyList(),
                timestamp = LocalDateTime.now()
            )
            
            rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                RabbitConfig.ORDER_CREATED,
                orderEvent
            )
            logger.info("Published order event: {} for order {}", eventType, order.orderNumber)
        } catch (e: Exception) {
            logger.error("Failed to publish order event", e)
        }
    }

    fun createOrder(request: CreateOrderRequest): OrderDto {
        logger.info("Creating order for user {}", request.userId)
        
        // Calculate totals
        val subtotal = request.items.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity)) }
        val taxAmount = subtotal.multiply(BigDecimal("0.1"))
        val shippingCost = BigDecimal("10.00")
        val finalAmount = subtotal.add(taxAmount).add(shippingCost)

        // Generate unique order number
        val orderNumber = "ORD-${System.currentTimeMillis()}"

        // Create order entity
        val order = Order(
            orderNumber = orderNumber,
            userId = request.userId,
            status = OrderStatus.PENDING,
            totalAmount = subtotal,
            shippingCost = shippingCost,
            taxAmount = taxAmount,
            finalAmount = finalAmount,
            shippingAddress = request.shippingAddress,
            billingAddress = request.billingAddress,
            paymentMethod = request.paymentMethod,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save order first
        val savedOrder = orderRepository.save(order)
        logger.info("Saved order to database: {}", savedOrder.orderNumber)

        // Create order items
        val orderItems = request.items.map { item ->
            OrderItem(
                order = savedOrder,
                productId = item.productId,
                productName = item.productName,
                productSku = item.productSku,
                unitPrice = item.unitPrice,
                quantity = item.quantity,
                totalPrice = item.unitPrice.multiply(BigDecimal(item.quantity))
            )
        }

        // Save order items
        val savedOrderItems = orderItemRepository.saveAll(orderItems)
        logger.info("Saved {} order items", savedOrderItems.size)

        // Publish events asynchronously
        publishOrderEvent(savedOrder, "ORDER_CREATED")

        // Return order DTO
        return OrderDto(
            id = savedOrder.id!!,
            orderNumber = savedOrder.orderNumber,
            userId = savedOrder.userId,
            status = savedOrder.status,
            totalAmount = savedOrder.totalAmount,
            shippingCost = savedOrder.shippingCost,
            taxAmount = savedOrder.taxAmount,
            finalAmount = savedOrder.finalAmount,
            items = savedOrderItems.map { item ->
                OrderItemDto(
                    id = item.id ?: UUID.randomUUID(),
                    productId = item.productId,
                    productName = item.productName,
                    productSku = item.productSku,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice
                )
            },
            shippingAddress = savedOrder.shippingAddress,
            billingAddress = savedOrder.billingAddress,
            paymentMethod = savedOrder.paymentMethod,
            paymentTransactionId = savedOrder.paymentTransactionId,
            createdAt = savedOrder.createdAt,
            updatedAt = savedOrder.updatedAt,
            completedAt = savedOrder.completedAt
        )
    }

    fun getOrderById(id: UUID): OrderDto? {
        logger.info("Getting order by ID: {}", id)
        val order = orderRepository.findById(id).orElse(null) ?: return null
        val items = orderItemRepository.findByOrderId(id)
        
        return createOrderDto(order, items)
    }

    fun getOrderByNumber(orderNumber: String): OrderDto? {
        logger.info("Getting order by number: {}", orderNumber)
        val order = orderRepository.findByOrderNumber(orderNumber) ?: return null
        val items = orderItemRepository.findByOrderId(order.id!!)
        
        return createOrderDto(order, items)
    }

    fun getOrdersByUserId(userId: UUID, pageable: Pageable): Page<OrderDto> {
        logger.info("Getting orders for user: {}", userId)
        val orders = orderRepository.findByUserId(userId)
        
        val orderDtos = orders.map { order ->
            val items = orderItemRepository.findByOrderId(order.id!!)
            createOrderDto(order, items)
        }
        
        // Simple pagination simulation - in real app, repository should handle this
        val startIndex = (pageable.pageNumber * pageable.pageSize).coerceAtMost(orderDtos.size)
        val endIndex = ((pageable.pageNumber + 1) * pageable.pageSize).coerceAtMost(orderDtos.size)
        val pageContent = if (startIndex < orderDtos.size) orderDtos.subList(startIndex, endIndex) else emptyList()
        
        return PageImpl(pageContent, pageable, orderDtos.size.toLong())
    }

    fun updateOrderStatus(id: UUID, request: UpdateOrderStatusRequest): OrderDto? {
        logger.info("Updating order {} status to {}", id, request.status)
        val order = orderRepository.findById(id).orElse(null) ?: return null
        
        // Update mutable fields
        order.status = request.status
        order.paymentTransactionId = request.paymentTransactionId ?: order.paymentTransactionId
        order.updatedAt = LocalDateTime.now()
        order.completedAt = if (request.status == OrderStatus.DELIVERED) LocalDateTime.now() else order.completedAt
        
        val savedOrder = orderRepository.save(order)
        
        // Publish order status update event asynchronously
        publishOrderEvent(savedOrder, "ORDER_STATUS_UPDATED")
        
        val items = orderItemRepository.findByOrderId(id)
        return createOrderDto(savedOrder, items)
    }

    private fun createOrderDto(order: Order, items: List<OrderItem>): OrderDto {
        return OrderDto(
            id = order.id!!,
            orderNumber = order.orderNumber,
            userId = order.userId,
            status = order.status,
            totalAmount = order.totalAmount,
            shippingCost = order.shippingCost,
            taxAmount = order.taxAmount,
            finalAmount = order.finalAmount,
            items = items.map { item ->
                OrderItemDto(
                    id = item.id ?: UUID.randomUUID(),
                    productId = item.productId,
                    productName = item.productName,
                    productSku = item.productSku,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    totalPrice = item.totalPrice
                )
            },
            shippingAddress = order.shippingAddress,
            billingAddress = order.billingAddress,
            paymentMethod = order.paymentMethod,
            paymentTransactionId = order.paymentTransactionId,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            completedAt = order.completedAt
        )
    }
}
