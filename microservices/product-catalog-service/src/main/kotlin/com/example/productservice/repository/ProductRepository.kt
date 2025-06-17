package com.example.productservice.repository

import com.example.productservice.model.Category
import com.example.productservice.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findByName(name: String): Category?
    fun findByParentCategoryIsNull(): List<Category>
    fun findByParentCategory(parentCategory: Category): List<Category>
}

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByCategory(category: Category): List<Product>
    fun findByCategoryId(categoryId: UUID): List<Product>
    fun findByNameContainingIgnoreCase(name: String): List<Product>
    fun findByIsActiveTrue(): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    fun findAvailableProducts(): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    fun findLowStockProducts(threshold: Int = 10): List<Product>
}
