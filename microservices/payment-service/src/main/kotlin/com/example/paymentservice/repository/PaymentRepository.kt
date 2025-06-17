package com.example.paymentservice.repository

import com.example.paymentservice.model.Payment
import com.example.paymentservice.model.PaymentStatus
import com.example.paymentservice.model.PaymentGateway
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {
    
    fun findByOrderId(orderId: UUID): List<Payment>
    
    fun findByUserId(userId: UUID): List<Payment>
    
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Payment>
    
    fun findByPaymentReference(paymentReference: String): Payment?
    
    fun findByGatewayTransactionId(gatewayTransactionId: String): Payment?
    
    fun findByStatus(status: PaymentStatus): List<Payment>
    
    fun findByStatusIn(statuses: List<PaymentStatus>): List<Payment>
    
    fun findByGateway(gateway: PaymentGateway): List<Payment>
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    fun findPaymentsByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Payment>
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    fun findByUserIdAndStatus(
        @Param("userId") userId: UUID,
        @Param("status") status: PaymentStatus
    ): List<Payment>
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    fun countByStatus(@Param("status") status: PaymentStatus): Long
    
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.status = 'PROCESSING' 
        AND p.createdAt < :cutoffTime
    """)
    fun findStuckPayments(@Param("cutoffTime") cutoffTime: LocalDateTime): List<Payment>
    
    @Query("""
        SELECT SUM(p.amount) FROM Payment p 
        WHERE p.status = 'COMPLETED' 
        AND p.processedAt BETWEEN :startDate AND :endDate
    """)
    fun getTotalRevenue(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): BigDecimal?
}
