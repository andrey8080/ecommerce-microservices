package com.example.orderservice.repository

import com.example.orderservice.model.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, UUID> {
    
    fun findByOrderId(orderId: UUID): List<OrderItem>
    
    fun findByProductId(productId: UUID): List<OrderItem>
    
    @Query("""
        SELECT oi FROM OrderItem oi 
        WHERE oi.productId = :productId 
        AND oi.order.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED')
    """)
    fun findActiveOrdersByProductId(@Param("productId") productId: UUID): List<OrderItem>
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    fun getTotalQuantitySoldForProduct(@Param("productId") productId: UUID): Int?
}
