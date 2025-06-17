package com.example.productservice.controller

import com.example.productservice.dto.CategoryDto
import com.example.productservice.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "API для управления категориями товаров")
class CategoryController(
    private val categoryService: CategoryService
) {

    @Operation(summary = "Создать новую категорию")
    @PostMapping
    fun createCategory(@Valid @RequestBody request: CategoryDto): ResponseEntity<CategoryDto> {
        val category = categoryService.createCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(category)
    }

    @Operation(summary = "Получить все категории")
    @GetMapping
    fun getAllCategories(): ResponseEntity<List<CategoryDto>> {
        val categories = categoryService.getAllCategories()
        return ResponseEntity.ok(categories)
    }

    @Operation(summary = "Получить категорию по ID")
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: UUID): ResponseEntity<CategoryDto> {
        val category = categoryService.getCategoryById(id)
        return if (category != null) {
            ResponseEntity.ok(category)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Обновить категорию")
    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CategoryDto
    ): ResponseEntity<CategoryDto> {
        return try {
            val category = categoryService.updateCategory(id, request)
            ResponseEntity.ok(category)
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/{id}")
    fun deleteCategory(@PathVariable id: UUID): ResponseEntity<Void> {
        return try {
            categoryService.deleteCategory(id)
            ResponseEntity.noContent().build()
        } catch (e: RuntimeException) {
            ResponseEntity.notFound().build()
        }
    }
}
