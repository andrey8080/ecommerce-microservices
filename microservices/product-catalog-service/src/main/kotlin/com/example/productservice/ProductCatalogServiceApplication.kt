package com.example.productservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
// @EnableDiscoveryClient
@EnableScheduling
class ProductCatalogServiceApplication

fun main(args: Array<String>) {
    runApplication<ProductCatalogServiceApplication>(*args)
}
