package com.example.orderservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication

@SpringBootApplication
// @EnableDiscoveryClient
@EnableScheduling
@EnableTransactionManagement
@EnableProcessApplication
class OrderServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
