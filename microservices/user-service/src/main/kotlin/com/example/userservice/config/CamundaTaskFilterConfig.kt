package com.example.userservice.config

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.filter.Filter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamundaTaskFilterConfig {

    @Bean
    fun taskFilterSetup(@Autowired processEngine: ProcessEngine) = ApplicationRunner {
        val filterService = processEngine.filterService
        
        try {
            // Проверяем, есть ли уже фильтр "All Tasks"
            val existingFilter = filterService.createFilterQuery()
                .filterName("All Tasks")
                .singleResult()
            
            if (existingFilter == null) {
                // Создаем фильтр для всех задач
                val allTasksFilter: Filter = filterService.newTaskFilter("All Tasks")
                allTasksFilter.owner = "demo"
                allTasksFilter.query = processEngine.taskService.createTaskQuery()
                
                // Устанавливаем свойства фильтра
                val properties = mutableMapOf<String, Any>()
                properties["color"] = "#3e4d2f"
                properties["description"] = "All available tasks"
                properties["priority"] = 10
                allTasksFilter.properties = properties
                
                filterService.saveFilter(allTasksFilter)
                println("Создан фильтр 'All Tasks' для пользователя demo")
            } else {
                println("Фильтр 'All Tasks' уже существует")
            }
            
            // Создаем фильтр для задач пользователя demo
            val demoTasksFilter = filterService.createFilterQuery()
                .filterName("My Tasks")
                .singleResult()
                
            if (demoTasksFilter == null) {
                val myTasksFilter: Filter = filterService.newTaskFilter("My Tasks")
                myTasksFilter.owner = "demo"
                myTasksFilter.query = processEngine.taskService.createTaskQuery()
                    .taskCandidateUser("demo")
                
                val myTasksProperties = mutableMapOf<String, Any>()
                myTasksProperties["color"] = "#2d4d3e"
                myTasksProperties["description"] = "Tasks assigned to me"
                myTasksProperties["priority"] = 5
                myTasksFilter.properties = myTasksProperties
                
                filterService.saveFilter(myTasksFilter)
                println("Создан фильтр 'My Tasks' для пользователя demo")
            } else {
                println("Фильтр 'My Tasks' уже существует")
            }
            
            // Выводим информацию о всех фильтрах
            val allFilters = filterService.createFilterQuery().list()
            println("=== Все фильтры в системе ===")
            allFilters.forEach { filter ->
                println("Фильтр: ${filter.name} (владелец: ${filter.owner})")
                println("  ID: ${filter.id}")
                println("  Свойства: ${filter.properties}")
            }
            
        } catch (e: Exception) {
            println("Ошибка при создании фильтров: ${e.message}")
            e.printStackTrace()
        }
    }
}
