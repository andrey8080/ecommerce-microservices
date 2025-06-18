package com.example.userservice.config

import org.camunda.bpm.engine.ProcessEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamundaProcessStarterConfig {

    @Bean
    fun processStarter(@Autowired processEngine: ProcessEngine) = ApplicationRunner {
        try {
            // Проверяем количество активных экземпляров процесса userRegistrationProcess
            val activeInstances = processEngine.runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey("userRegistrationProcess")
                .active()
                .count()
            
            println("Активных экземпляров userRegistrationProcess: $activeInstances")
            
            // Если активных экземпляров мало, запускаем новые для тестирования
            if (activeInstances < 2) {
                val instancesToCreate = 2 - activeInstances.toInt()
                
                repeat(instancesToCreate) { i ->
                    val processInstance = processEngine.runtimeService
                        .startProcessInstanceByKey("userRegistrationProcess")
                    
                    println("Запущен экземпляр процесса: ${processInstance.id}")
                    
                    // Проверяем созданные задачи
                    val tasks = processEngine.taskService.createTaskQuery()
                        .processInstanceId(processInstance.id)
                        .list()
                    
                    tasks.forEach { task ->
                        println("  Создана задача: ${task.name} (ID: ${task.id})")
                        val candidateUsers = processEngine.taskService
                            .getIdentityLinksForTask(task.id)
                            .filter { it.type == "candidate" && it.userId != null }
                            .map { it.userId }
                        val candidateGroups = processEngine.taskService
                            .getIdentityLinksForTask(task.id)
                            .filter { it.type == "candidate" && it.groupId != null }
                            .map { it.groupId }
                        println("    Кандидаты-пользователи: $candidateUsers")
                        println("    Кандидаты-группы: $candidateGroups")
                    }
                }
            }
            
            // Выводим общую статистику
            val allTasks = processEngine.taskService.createTaskQuery().list()
            val demoTasks = processEngine.taskService.createTaskQuery()
                .taskCandidateUser("demo")
                .list()
            val groupTasks = processEngine.taskService.createTaskQuery()
                .taskCandidateGroup("users")
                .list()
            
            println("=== Статистика задач ===")
            println("Всего задач в системе: ${allTasks.size}")
            println("Задач для пользователя 'demo': ${demoTasks.size}")
            println("Задач для группы 'users': ${groupTasks.size}")
            
            // Детальная информация о задачах
            allTasks.forEach { task ->
                println("Задача: ${task.name}")
                println("  ID: ${task.id}")
                println("  Процесс: ${task.processInstanceId}")
                println("  Назначена: ${task.assignee ?: "не назначена"}")
                
                // Получаем кандидатов через отдельные запросы
                val candidateUsers = processEngine.taskService
                    .getIdentityLinksForTask(task.id)
                    .filter { it.type == "candidate" && it.userId != null }
                    .map { it.userId }
                    
                val candidateGroups = processEngine.taskService
                    .getIdentityLinksForTask(task.id)
                    .filter { it.type == "candidate" && it.groupId != null }
                    .map { it.groupId }
                
                println("  Кандидаты-пользователи: $candidateUsers")
                println("  Кандидаты-группы: $candidateGroups")
                println()
            }
            
        } catch (e: Exception) {
            println("Ошибка при запуске процессов: ${e.message}")
            e.printStackTrace()
        }
    }
}
