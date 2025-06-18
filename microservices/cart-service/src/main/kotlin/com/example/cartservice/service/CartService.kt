package com.example.cartservice.service

import com.example.cartservice.config.RabbitConfig
import com.example.cartservice.dto.*
import com.example.cartservice.model.Cart
import com.example.cartservice.model.CartItem
import com.example.cartservice.repository.CartRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.camunda.bpm.engine.RuntimeService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class CartService(
    private val cartRepository: CartRepository,
    private val rabbitTemplate: RabbitTemplate,
    private val runtimeService: RuntimeService
) {
    private val logger = LoggerFactory.getLogger(CartService::class.java)

    fun getCartByUserId(userId: String): CartDto? {
        val cart = cartRepository.findActiveCartByUserId(userId)
        return cart?.let { convertToDto(it) }
    }

    fun addItemToCart(userId: String, request: AddToCartRequest): CartDto {
        val cart = cartRepository.findActiveCartByUserId(userId) ?: Cart(userId = userId)
        
        val itemKey = request.productId
        val existingItem = cart.items[itemKey]
        
        if (existingItem != null) {
            // Update quantity if item already exists
            cart.items[itemKey] = existingItem.copy(quantity = existingItem.quantity + request.quantity)
        } else {
            // Add new item
            cart.items[itemKey] = CartItem(
                productId = request.productId,
                productName = request.productName,
                price = request.price,
                quantity = request.quantity
            )
        }
        
        val updatedCart = cart.copy(updatedAt = LocalDateTime.now())
        cartRepository.save(updatedCart)
        
        // Publish event
        publishCartEvent(CartEvent(
            eventType = RabbitConfig.CART_ITEM_ADDED,
            userId = userId,
            cartId = updatedCart.id,
            productId = request.productId,
            quantity = request.quantity
        ))
        
        logger.info("Added item {} to cart for user {}", request.productId, userId)
        return convertToDto(updatedCart)
    }

    fun updateCartItem(userId: String, productId: String, request: UpdateCartItemRequest): CartDto? {
        val cart = cartRepository.findActiveCartByUserId(userId) ?: return null
        val itemKey = productId
        
        if (request.quantity <= 0) {
            cart.items.remove(itemKey)
            publishCartEvent(CartEvent(
                eventType = RabbitConfig.CART_ITEM_REMOVED,
                userId = userId,
                cartId = cart.id,
                productId = productId
            ))
        } else {
            cart.items[itemKey]?.let { existingItem ->
                cart.items[itemKey] = existingItem.copy(quantity = request.quantity)
                publishCartEvent(CartEvent(
                    eventType = RabbitConfig.CART_ITEM_UPDATED,
                    userId = userId,
                    cartId = cart.id,
                    productId = productId,
                    quantity = request.quantity
                ))
            }
        }
        
        val updatedCart = cart.copy(updatedAt = LocalDateTime.now())
        cartRepository.save(updatedCart)
        
        logger.info("Updated item {} in cart for user {}", productId, userId)
        return convertToDto(updatedCart)
    }

    fun removeItemFromCart(userId: String, productId: String): CartDto? {
        val cart = cartRepository.findActiveCartByUserId(userId) ?: return null
        val itemKey = productId
        
        cart.items.remove(itemKey)
        val updatedCart = cart.copy(updatedAt = LocalDateTime.now())
        cartRepository.save(updatedCart)
        
        publishCartEvent(CartEvent(
            eventType = RabbitConfig.CART_ITEM_REMOVED,
            userId = userId,
            cartId = updatedCart.id,
            productId = productId
        ))
        
        logger.info("Removed item {} from cart for user {}", productId, userId)
        return convertToDto(updatedCart)
    }

    fun clearCart(userId: String): Boolean {
        val cart = cartRepository.findActiveCartByUserId(userId) ?: return false
        
        cart.items.clear()
        val updatedCart = cart.copy(updatedAt = LocalDateTime.now())
        cartRepository.save(updatedCart)
        
        publishCartEvent(CartEvent(
            eventType = RabbitConfig.CART_CLEARED,
            userId = userId,
            cartId = updatedCart.id
        ))
        
        logger.info("Cleared cart for user {}", userId)
        return true
    }

    @RabbitListener(queues = [RabbitConfig.ORDER_CREATED_QUEUE])
    fun handleOrderCreated(orderEvent: Map<String, Any>) {
        try {
            val userId = orderEvent["userId"] as String
            val variables = mapOf("userId" to userId)
            runtimeService.startProcessInstanceByKey("cartProcess", variables)
        } catch (e: Exception) {
            logger.error("Error processing order created event", e)
        }
    }

    fun generateCartStatistics() {
        logger.info("Generating cart statistics")
        
        try {
            val allCarts = cartRepository.findAll()
            val activeCarts = allCarts.filter { it.items.isNotEmpty() }
            val totalItems = activeCarts.sumOf { it.getTotalItems() }
            val totalValue = activeCarts.sumOf { it.getTotalPrice() }
            
            logger.info("Cart Statistics - Active carts: {}, Total items: {}, Total value: {}", 
                activeCarts.size, totalItems, totalValue)
        } catch (e: Exception) {
            logger.error("Error generating cart statistics", e)
        }
    }

    private fun publishCartEvent(event: CartEvent) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitConfig.CART_EXCHANGE,
                event.eventType,
                event
            )
        } catch (e: Exception) {
            logger.error("Failed to publish cart event", e)
        }
    }

    private fun convertToDto(cart: Cart): CartDto {
        return CartDto(
            id = cart.id,
            userId = cart.userId,
            items = cart.items.values.map { item ->
                CartItemDto(
                    productId = item.productId,
                    productName = item.productName,
                    price = item.price,
                    quantity = item.quantity
                )
            },
            totalPrice = cart.getTotalPrice(),
            totalItems = cart.getTotalItems(),
            createdAt = cart.createdAt,
            updatedAt = cart.updatedAt
        )
    }
}
