package com.example.cartservice.repository

import com.example.cartservice.model.Cart
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CartRepository : CassandraRepository<Cart, String> {
    
    @Query("SELECT * FROM carts WHERE userid = ?0 ALLOW FILTERING")
    fun findByUserId(userId: String): List<Cart>
    
    @Query("SELECT * FROM carts WHERE userid = ?0 LIMIT 1 ALLOW FILTERING")
    fun findActiveCartByUserId(userId: String): Cart?
    
    @Query("DELETE FROM carts WHERE userid = ?0")
    fun deleteByUserId(userId: String)
}
