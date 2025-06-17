package com.example.productservice.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.util.*

data class CategoryDto(
    val id: UUID? = null,
    @field:NotBlank(message = "Category name is required")
    val name: String,
    val description: String? = null,
    val parentCategoryId: UUID? = null,
    val childCategories: List<CategoryDto> = emptyList()
)

data class ProductDto(
    val id: UUID? = null,
    @field:NotBlank(message = "Product name is required")
    val name: String,
    val description: String? = null,
    @field:NotNull(message = "Price is required")
    @field:PositiveOrZero(message = "Price must be positive or zero")
    val price: BigDecimal,
    @field:NotNull(message = "Stock quantity is required")
    @field:PositiveOrZero(message = "Stock quantity must be positive or zero")
    val stockQuantity: Int,
    @field:NotNull(message = "Category ID is required")
    val categoryId: UUID,
    val brand: String? = null,
    val sku: String? = null,
    val isActive: Boolean = true
)

data class ProductEvent(
    val eventType: String,
    val productId: UUID,
    val productDto: ProductDto
)

data class StockUpdateEvent(
    val productId: UUID,
    val oldQuantity: Int,
    val newQuantity: Int,
    val updateType: String // "INCREASE", "DECREASE", "SET"
)
