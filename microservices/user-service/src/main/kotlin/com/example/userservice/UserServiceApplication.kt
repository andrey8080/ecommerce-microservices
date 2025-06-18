package com.example.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication

@SpringBootApplication
// @EnableDiscoveryClient
@EnableScheduling
@EnableProcessApplication
class UserServiceApplication

fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
