package com.example.apigateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Bean
    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // User Service Routes
            .route("user-service") { r ->
                r.path("/api/users/**")
                    .uri("http://user-service:8080")
            }
            // Product Catalog Routes
            .route("product-catalog-service") { r ->
                r.path("/api/products/**")
                    .uri("http://product-catalog-service:8080")
            }
            // Cart Service Routes
            .route("cart-service") { r ->
                r.path("/api/v1/cart/**")
                    .uri("http://cart-service:8080")
            }
            // Order Service Routes
            .route("order-service") { r ->
                r.path("/api/orders/**")
                    .uri("http://order-service:8080")
            }
            // Payment Service Routes
            .route("payment-service") { r ->
                r.path("/api/payments/**")
                    .uri("http://payment-service:8080")
            }
            .build()
    }
}
