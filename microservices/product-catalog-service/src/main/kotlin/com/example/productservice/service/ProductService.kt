package com.example.productservice.service

import com.example.productservice.config.RabbitConfig
import com.example.productservice.dto.*
import com.example.productservice.model.Category
import com.example.productservice.model.Product
import com.example.productservice.repository.CategoryRepository
import com.example.productservice.repository.ProductRepository
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val rabbitTemplate: RabbitTemplate
) {

    fun createProduct(request: ProductDto): ProductDto {
        val category = categoryRepository.findById(request.categoryId).orElseThrow {
            RuntimeException("Category not found with id: ${request.categoryId}")
        }

        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity,
            category = category,
            brand = request.brand,
            sku = request.sku,
            isActive = request.isActive
        )

        val savedProduct = productRepository.save(product)
        val productDto = savedProduct.toDto()

        // Отправляем асинхронное сообщение о создании продукта
        publishProductEvent("PRODUCT_CREATED", savedProduct.id, productDto)

        return productDto
    }

    fun getProductById(id: UUID): ProductDto? {
        return productRepository.findById(id).orElse(null)?.toDto()
    }

    fun getProductsByCategory(categoryId: UUID): List<ProductDto> {
        return productRepository.findByCategoryId(categoryId).map { it.toDto() }
    }

    fun searchProducts(query: String): List<ProductDto> {
        return productRepository.findByNameContainingIgnoreCase(query).map { it.toDto() }
    }

    fun getAllProducts(): List<ProductDto> {
        return productRepository.findAll().map { it.toDto() }
    }

    fun updateProduct(id: UUID, request: ProductDto): ProductDto {
        val existingProduct = productRepository.findById(id).orElseThrow {
            RuntimeException("Product not found with id: $id")
        }

        val category = categoryRepository.findById(request.categoryId).orElseThrow {
            RuntimeException("Category not found with id: ${request.categoryId}")
        }

        val updatedProduct = existingProduct.copy(
            name = request.name,
            description = request.description,
            price = request.price,
            stockQuantity = request.stockQuantity,
            category = category,
            brand = request.brand,
            sku = request.sku,
            isActive = request.isActive
        )

        val savedProduct = productRepository.save(updatedProduct)
        val productDto = savedProduct.toDto()

        publishProductEvent("PRODUCT_UPDATED", savedProduct.id, productDto)

        return productDto
    }

    fun updateStock(productId: UUID, newQuantity: Int): ProductDto {
        val product = productRepository.findById(productId).orElseThrow {
            RuntimeException("Product not found with id: $productId")
        }

        val oldQuantity = product.stockQuantity
        val updatedProduct = product.copy(stockQuantity = newQuantity)
        val savedProduct = productRepository.save(updatedProduct)

        // Отправляем событие об изменении запасов
        val stockEvent = StockUpdateEvent(
            productId = productId,
            oldQuantity = oldQuantity,
            newQuantity = newQuantity,
            updateType = when {
                newQuantity > oldQuantity -> "INCREASE"
                newQuantity < oldQuantity -> "DECREASE"
                else -> "SET"
            }
        )

        rabbitTemplate.convertAndSend(RabbitConfig.PRODUCT_EXCHANGE, "stock.updated", stockEvent)

        // Проверяем низкий уровень запасов
        if (newQuantity <= 10) {
            rabbitTemplate.convertAndSend(RabbitConfig.PRODUCT_EXCHANGE, "stock.low_alert", stockEvent)
        }

        return savedProduct.toDto()
    }

    fun deleteProduct(id: UUID) {
        val product = productRepository.findById(id).orElseThrow {
            RuntimeException("Product not found with id: $id")
        }

        productRepository.delete(product)
        publishProductEvent("PRODUCT_DELETED", id, product.toDto())
    }

    // Обработка заказов - резервирование запасов
    @RabbitListener(queues = ["stock.reservation"])
    fun handleReserveStock(request: Map<String, Any>) {
        println("========== RECEIVED STOCK RESERVATION REQUEST ==========")
        println("Request: $request")
        
        val orderId = UUID.fromString(request["orderId"] as String)
        val items = request["items"] as List<Map<String, Any>>

        println("Order ID: $orderId")
        println("Items: $items")

        try {
            var allReserved = true
            val failedItems = mutableListOf<Map<String, Any>>()
            
            for (item in items) {
                val productId = UUID.fromString(item["productId"] as String)
                val quantity = (item["quantity"] as Number).toInt()
                
                println("Processing item - Product ID: $productId, Quantity: $quantity")
                
                val product = productRepository.findById(productId).orElse(null)
                if (product == null) {
                    println("Product not found: $productId")
                    allReserved = false
                    failedItems.add(mapOf("productId" to productId, "quantity" to quantity))
                    continue
                }
                
                println("Product found: ${product.name}, Current stock: ${product.stockQuantity}")
                
                if (product.stockQuantity >= quantity) {
                    updateStock(productId, product.stockQuantity - quantity)
                    println("Stock reserved successfully for product $productId")
                } else {
                    println("Insufficient stock for product $productId. Available: ${product.stockQuantity}, Requested: $quantity")
                    allReserved = false
                    failedItems.add(mapOf("productId" to productId, "quantity" to quantity))
                }
            }
            
            // Отправляем ответ
            val response = mapOf(
                "orderId" to orderId,
                "success" to allReserved,
                "message" to if (allReserved) "All items reserved successfully" else "Some items could not be reserved",
                "failedItems" to failedItems
            )
            
            println("Sending response: $response")
            rabbitTemplate.convertAndSend("stock.exchange", "stock.reservation.response", response)
            println("Response sent successfully")
            
        } catch (e: Exception) {
            println("ERROR processing stock reservation: ${e.message}")
            e.printStackTrace()
            
            val errorEvent = mapOf(
                "orderId" to orderId,
                "success" to false,
                "message" to "Error processing stock reservation: ${e.message}",
                "failedItems" to items.map { item ->
                    mapOf(
                        "productId" to item["productId"],
                        "quantity" to item["quantity"]
                    )
                }
            )
            
            rabbitTemplate.convertAndSend("stock.exchange", "stock.reservation.response", errorEvent)
        }
    }

    // Освобождение запасов при отмене заказа
    @RabbitListener(queues = ["stock.release"])
    fun handleReleaseStock(orderItem: Map<String, Any>) {
        val productId = UUID.fromString(orderItem["productId"] as String)
        val quantity = orderItem["quantity"] as Int

        val product = productRepository.findById(productId).orElse(null)
        if (product != null) {
            updateStock(productId, product.stockQuantity + quantity)
        }
    }

    private fun publishProductEvent(eventType: String, productId: UUID, productDto: ProductDto) {
        val event = ProductEvent(
            eventType = eventType,
            productId = productId,
            productDto = productDto
        )

        val routingKey = when (eventType) {
            "PRODUCT_CREATED" -> "product.created"
            "PRODUCT_UPDATED" -> "product.updated"
            "PRODUCT_DELETED" -> "product.deleted"
            else -> "product.unknown"
        }

        rabbitTemplate.convertAndSend(RabbitConfig.PRODUCT_EXCHANGE, routingKey, event)
    }

    private fun Product.toDto(): ProductDto {
        return ProductDto(
            id = this.id,
            name = this.name,
            description = this.description,
            price = this.price,
            stockQuantity = this.stockQuantity,
            categoryId = this.category.id,
            brand = this.brand,
            sku = this.sku,
            isActive = this.isActive
        )
    }

    // Планировщик задач - проверка низкого уровня запасов
    @Scheduled(cron = "0 0 9 * * ?") // Каждый день в 9:00
    fun checkLowStockProducts() {
        val lowStockProducts = productRepository.findLowStockProducts(10)
        
        lowStockProducts.forEach { product ->
            val alertEvent = mapOf(
                "productId" to product.id,
                "productName" to product.name,
                "currentStock" to product.stockQuantity,
                "alertType" to "LOW_STOCK_DAILY_CHECK"
            )
            
            rabbitTemplate.convertAndSend(RabbitConfig.PRODUCT_EXCHANGE, "stock.low_alert", alertEvent)
        }
        
        println("Daily low stock check completed. Found ${lowStockProducts.size} products with low stock")
    }

    // Планировщик задач - обновление цен (интеграция с внешней системой)
    @Scheduled(fixedRate = 21600000) // Каждые 6 часов
    fun updatePricesFromExternalSystem() {
        println("Running scheduled price update from external system")
        
        // Здесь можно интегрироваться с внешней системой ценообразования
        // Через JCA или REST API
        
        val priceUpdateEvent = mapOf(
            "eventType" to "PRICE_UPDATE_SCHEDULED",
            "timestamp" to System.currentTimeMillis(),
            "productsUpdated" to 0
        )
        
        rabbitTemplate.convertAndSend(RabbitConfig.PRODUCT_EXCHANGE, "price.update_scheduled", priceUpdateEvent)
    }
}
