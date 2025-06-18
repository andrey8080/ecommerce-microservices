package com.example.paymentservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableAsync
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication

@SpringBootApplication
// @EnableDiscoveryClient
@EnableAsync
@EnableProcessApplication
class PaymentServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentServiceApplication>(*args)
}
