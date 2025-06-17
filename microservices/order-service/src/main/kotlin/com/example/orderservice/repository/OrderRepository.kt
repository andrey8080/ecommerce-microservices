package com.example.orderservice.repository

import com.example.orderservice.model.Order
import com.example.orderservice.model.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    
    fun findByUserId(userId: UUID): List<Order>
    
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Order>
    
    fun findByOrderNumber(orderNumber: String): Order?
    
    fun findByStatus(status: OrderStatus): List<Order>
    
    fun findByStatusIn(statuses: List<OrderStatus>): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    fun findOrdersByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status")
    fun findByUserIdAndStatus(
        @Param("userId") userId: UUID,
        @Param("status") status: OrderStatus
    ): List<Order>
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    fun countByStatus(@Param("status") status: OrderStatus): Long
    
    @Query("""
        SELECT o FROM Order o 
        WHERE o.status IN ('PENDING', 'CONFIRMED') 
        AND o.createdAt < :cutoffTime
    """)
    fun findStaleOrders(@Param("cutoffTime") cutoffTime: LocalDateTime): List<Order>
}
