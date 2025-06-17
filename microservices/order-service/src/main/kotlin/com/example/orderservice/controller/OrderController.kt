package com.example.orderservice.controller

import com.example.orderservice.dto.CreateOrderRequest
import com.example.orderservice.dto.OrderDto
import com.example.orderservice.dto.UpdateOrderStatusRequest
import com.example.orderservice.service.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management")
class OrderController(private val orderService: OrderService) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(OrderController::class.java)
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order with items")
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<Any> {
        return try {
            logger.info("Received create order request: {}", request)
            val order = orderService.createOrder(request)
            logger.info("Order created successfully: {}", order.id)
            ResponseEntity.status(HttpStatus.CREATED).body(order)
        } catch (e: RuntimeException) {
            logger.error("RuntimeException in createOrder: {}", e.message, e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "Bad request", "message" to (e.message ?: "Invalid request data")))
        } catch (e: Exception) {
            logger.error("Exception in createOrder: {}", e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Internal server error", "message" to (e.message ?: "An unexpected error occurred")))
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by ID")
    fun getOrderById(@PathVariable orderId: UUID): ResponseEntity<OrderDto> {
        val order = orderService.getOrderById(orderId)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Retrieve order details by order number")
    fun getOrderByNumber(@PathVariable orderNumber: String): ResponseEntity<OrderDto> {
        val order = orderService.getOrderByNumber(orderNumber)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user", description = "Retrieve paginated orders for a specific user")
    fun getOrdersByUserId(
        @PathVariable userId: UUID,
        pageable: Pageable
    ): ResponseEntity<Page<OrderDto>> {
        val orders = orderService.getOrdersByUserId(userId, pageable)
        return ResponseEntity.ok(orders)
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    fun updateOrderStatus(
        @PathVariable orderId: UUID,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderDto> {
        val order = orderService.updateOrderStatus(orderId, request)
        return if (order != null) {
            ResponseEntity.ok(order)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
