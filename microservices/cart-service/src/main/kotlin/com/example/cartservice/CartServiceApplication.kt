package com.example.cartservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication

@SpringBootApplication
@EnableScheduling
@EnableCassandraRepositories
@EnableProcessApplication
class CartServiceApplication

fun main(args: Array<String>) {
    runApplication<CartServiceApplication>(*args)
}
