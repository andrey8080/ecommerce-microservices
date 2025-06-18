package com.example.userservice.controller

import org.camunda.bpm.engine.ProcessEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/debug")
class DebugController {

    @Autowired
    lateinit var processEngine: ProcessEngine

    @GetMapping("/tasks")
    fun getAllTasks(): Map<String, Any> {
        val allTasks = processEngine.taskService.createTaskQuery().list()
        val demoTasks = processEngine.taskService.createTaskQuery()
            .taskCandidateUser("demo")
            .list()
        
        val result = mutableMapOf<String, Any>()
        result["totalTasks"] = allTasks.size
        result["demoTasks"] = demoTasks.size
        result["allTasksDetails"] = allTasks.map { task ->
            mapOf(
                "id" to task.id,
                "name" to task.name,
                "assignee" to task.assignee,
                "processInstanceId" to task.processInstanceId,
                "processDefinitionId" to task.processDefinitionId
            )
        }
        
        return result
    }

    @GetMapping("/processes")
    fun getAllProcesses(): Map<String, Any> {
        val processInstances = processEngine.runtimeService.createProcessInstanceQuery().list()
        
        return mapOf(
            "totalProcesses" to processInstances.size,
            "processes" to processInstances.map { instance ->
                mapOf(
                    "id" to instance.id,
                    "processDefinitionId" to instance.processDefinitionId,
                    "businessKey" to (instance.businessKey ?: "")
                )
            }
        )
    }

    @PostMapping("/start-process")
    fun startUserRegistrationProcess(): Map<String, Any> {
        return try {
            val processInstance = processEngine.runtimeService
                .startProcessInstanceByKey("userRegistrationProcess")
            
            val tasks = processEngine.taskService.createTaskQuery()
                .processInstanceId(processInstance.id)
                .list()
            
            mapOf(
                "success" to true,
                "processInstanceId" to processInstance.id,
                "tasksCreated" to tasks.size,
                "tasks" to tasks.map { task ->
                    mapOf(
                        "id" to task.id,
                        "name" to task.name
                    )
                }
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to e.message
            )
        }
    }
}
