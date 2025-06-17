package com.example.cartservice.controller

import com.example.cartservice.dto.AddToCartRequest
import com.example.cartservice.dto.CartDto
import com.example.cartservice.dto.UpdateCartItemRequest
import com.example.cartservice.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management")
class CartController(private val cartService: CartService) {

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's cart", description = "Retrieve the shopping cart for a specific user")
    fun getCart(@PathVariable userId: String): ResponseEntity<CartDto> {
        val cart = cartService.getCartByUserId(userId)
        return if (cart != null) {
            ResponseEntity.ok(cart)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the user's shopping cart")
    fun addItemToCart(
        @PathVariable userId: String,
        @Valid @RequestBody request: AddToCartRequest
    ): ResponseEntity<CartDto> {
        val cart = cartService.addItemToCart(userId, request)
        return ResponseEntity.ok(cart)
    }

    @PutMapping("/{userId}/items/{productId}")
    @Operation(summary = "Update cart item", description = "Update the quantity of a specific item in the cart")
    fun updateCartItem(
        @PathVariable userId: String,
        @PathVariable productId: String,
        @Valid @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<CartDto> {
        val cart = cartService.updateCartItem(userId, productId, request)
        return if (cart != null) {
            ResponseEntity.ok(cart)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the user's cart")
    fun removeItemFromCart(
        @PathVariable userId: String,
        @PathVariable productId: String
    ): ResponseEntity<CartDto> {
        val cart = cartService.removeItemFromCart(userId, productId)
        return if (cart != null) {
            ResponseEntity.ok(cart)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Clear cart", description = "Remove all items from the user's cart")
    fun clearCart(@PathVariable userId: String): ResponseEntity<Void> {
        val success = cartService.clearCart(userId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
