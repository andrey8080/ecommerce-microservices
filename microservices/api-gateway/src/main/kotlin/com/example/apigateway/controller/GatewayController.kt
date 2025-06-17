package com.example.apigateway.controller

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/gateway")
class GatewayController(
    private val routeLocator: RouteLocator
) {

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        val response = mapOf(
            "status" to "UP",
            "service" to "api-gateway",
            "timestamp" to LocalDateTime.now(),
            "version" to "1.0.0"
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/info")
    fun info(): ResponseEntity<Map<String, Any>> {
        val response = mapOf(
            "name" to "API Gateway",
            "description" to "Централизованная точка входа для микросервисной системы",
            "version" to "1.0.0",
            "features" to listOf(
                "Request routing",
                "Load balancing",
                "Circuit breaker",
                "Rate limiting",
                "CORS support"
            ),
            "routes" to getRouteInfo()
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/routes")
    fun routes(): ResponseEntity<Map<String, Any>> {
        val routes = getRouteInfo()
        return ResponseEntity.ok(mapOf("routes" to routes))
    }

    private fun getRouteInfo(): List<Map<String, String>> {
        return listOf(
            mapOf(
                "id" to "user-service",
                "path" to "/api/users/**",
                "uri" to "http://user-service:8080"
            ),
            mapOf(
                "id" to "product-catalog-service",
                "path" to "/api/products/**",
                "uri" to "http://product-catalog-service:8080"
            ),
            mapOf(
                "id" to "cart-service",
                "path" to "/api/v1/cart/**",
                "uri" to "http://cart-service:8080"
            ),
            mapOf(
                "id" to "order-service",
                "path" to "/api/orders/**",
                "uri" to "http://order-service:8080"
            ),
            mapOf(
                "id" to "payment-service",
                "path" to "/api/payments/**",
                "uri" to "http://payment-service:8080"
            )
        )
    }
}
