package com.example.orderservice.controller

import org.camunda.bpm.engine.RuntimeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.example.orderservice.dto.StartOrderProcessRequest

@RestController
@RequestMapping("/api/v1/process")
class ProcessController(private val runtimeService: RuntimeService) {

    @PostMapping("/start-order")
    fun startOrderProcess(@RequestBody request: StartOrderProcessRequest): ResponseEntity<Map<String, String>> {
        val variables = mapOf("userId" to request.userId.toString())
        val instance = runtimeService.startProcessInstanceByKey("orderPaymentProcess", variables)
        return ResponseEntity.ok(mapOf("instanceId" to instance.id))
    }
}
