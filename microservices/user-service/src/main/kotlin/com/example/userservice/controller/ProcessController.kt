package com.example.userservice.controller

import com.example.userservice.dto.StartUserProcessRequest
import org.camunda.bpm.engine.RuntimeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/process")
class ProcessController(private val runtimeService: RuntimeService) {

    @PostMapping("/start-user")
    fun startUserProcess(@RequestBody request: StartUserProcessRequest): ResponseEntity<Map<String, String>> {
        val variables = mapOf(
            "username" to request.username,
            "email" to request.email,
            "phoneNumber" to request.phoneNumber,
            "password" to request.password,
            "role" to request.role.name
        )
        val instance = runtimeService.startProcessInstanceByKey("userRegistrationProcess", variables)
        return ResponseEntity.ok(mapOf("instanceId" to instance.id))
    }
}
