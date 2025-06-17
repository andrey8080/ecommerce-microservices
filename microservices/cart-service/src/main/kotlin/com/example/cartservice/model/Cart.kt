package com.example.cartservice.model

import org.springframework.data.cassandra.core.mapping.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Table("carts")
data class Cart(
    @field:PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @field:Column("userid")
    val userId: String,
    @field:Column("items")
    @field:CassandraType(type = CassandraType.Name.MAP, typeArguments = [CassandraType.Name.TEXT, CassandraType.Name.UDT], userTypeName = "cart_item")
    val items: MutableMap<String, CartItem> = mutableMapOf(),
    @field:Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @field:Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getTotalPrice(): BigDecimal {
        return items.values.sumOf { it.price * it.quantity.toBigDecimal() }
    }
    
    fun getTotalItems(): Int {
        return items.values.sumOf { it.quantity }
    }
}

@UserDefinedType("cart_item")
data class CartItem(
    @field:Column("product_id")
    @field:CassandraType(type = CassandraType.Name.TEXT)
    val productId: String,
    @field:Column("product_name")
    @field:CassandraType(type = CassandraType.Name.TEXT)
    val productName: String,
    @field:Column("price")
    @field:CassandraType(type = CassandraType.Name.DECIMAL)
    val price: BigDecimal,
    @field:Column("quantity")
    @field:CassandraType(type = CassandraType.Name.INT)
    val quantity: Int
)
