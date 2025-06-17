package com.example.productservice.controller

import com.example.productservice.dto.ProductDto
import com.example.productservice.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Catalog", description = "API для управления каталогом товаров")
class ProductController(
    private val productService: ProductService
) {

    @Operation(summary = "Создать новый продукт")
    @PostMapping
    fun createProduct(@Valid @RequestBody request: ProductDto): ResponseEntity<ProductDto> {
        val product = productService.createProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @Operation(summary = "Получить все продукты")
    @GetMapping
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) category: UUID?
    ): ResponseEntity<Page<ProductDto>> {
        val pageable: Pageable = PageRequest.of(page, size)
        
        val products = when {
            !search.isNullOrBlank() -> {
                // Поиск по названию - возвращаем как страницу
                val searchResults = productService.searchProducts(search)
                val start = page * size
                val end = minOf(start + size, searchResults.size)
                val pageContent = if (start < searchResults.size) searchResults.subList(start, end) else emptyList()
                org.springframework.data.domain.PageImpl(pageContent, pageable, searchResults.size.toLong())
            }
            category != null -> {
                // Поиск по категории - возвращаем как страницу
                val categoryResults = productService.getProductsByCategory(category)
                val start = page * size
                val end = minOf(start + size, categoryResults.size)
                val pageContent = if (start < categoryResults.size) categoryResults.subList(start, end) else emptyList()
                org.springframework.data.domain.PageImpl(pageContent, pageable, categoryResults.size.toLong())
            }
            else -> {
                // Все продукты - возвращаем как страницу
                val allProducts = productService.getAllProducts()
                val start = page * size
                val end = minOf(start + size, allProducts.size)
                val pageContent = if (start < allProducts.size) allProducts.subList(start, end) else emptyList()
                org.springframework.data.domain.PageImpl(pageContent, pageable, allProducts.size.toLong())
            }
        }
        
        return ResponseEntity.ok(products)
    }

    @Operation(summary = "Получить продукт по ID")
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: UUID): ResponseEntity<ProductDto> {
        val product = productService.getProductById(id)
        return if (product != null) {
            ResponseEntity.ok(product)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Обновить продукт")
    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ProductDto
    ): ResponseEntity<ProductDto> {
        return try {
            val product = productService.updateProduct(id, request)
            ResponseEntity.ok(product)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Удалить продукт")
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: UUID): ResponseEntity<Void> {
        return try {
            productService.deleteProduct(id)
            ResponseEntity.noContent().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Обновить запасы продукта")
    @PutMapping("/{id}/stock")
    fun updateStock(
        @PathVariable id: UUID,
        @RequestBody request: StockUpdateRequest
    ): ResponseEntity<ProductDto> {
        return try {
            val product = productService.updateStock(id, request.quantity)
            ResponseEntity.ok(product)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }
}

data class StockUpdateRequest(
    val quantity: Int,
    val operation: String = "SET"
)
