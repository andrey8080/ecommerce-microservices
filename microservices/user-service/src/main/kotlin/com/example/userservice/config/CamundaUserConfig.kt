package com.example.userservice.config

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.identity.Group
import org.camunda.bpm.engine.identity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamundaUserConfig {

    @Bean
    fun camundaUserSetup(@Autowired processEngine: ProcessEngine) = ApplicationRunner {
        val identityService = processEngine.identityService
        
        // Создаем пользователя demo если его еще нет
        val existingUser = identityService.createUserQuery().userId("demo").singleResult()
        if (existingUser == null) {
            val user: User = identityService.newUser("demo")
            user.firstName = "Demo"
            user.lastName = "User"
            user.email = "demo@example.com"
            user.password = "demo"
            identityService.saveUser(user)
            println("Создан пользователь demo")
        } else {
            println("Пользователь demo уже существует")
        }
        
        // Создаем группу camunda-admin если ее еще нет
        val existingAdminGroup = identityService.createGroupQuery().groupId("camunda-admin").singleResult()
        if (existingAdminGroup == null) {
            val adminGroup: Group = identityService.newGroup("camunda-admin")
            adminGroup.name = "Camunda Administrators"
            adminGroup.type = "SYSTEM"
            identityService.saveGroup(adminGroup)
            println("Создана группа camunda-admin")
        } else {
            println("Группа camunda-admin уже существует")
        }
        
        // Создаем группу users если ее еще нет
        val existingUsersGroup = identityService.createGroupQuery().groupId("users").singleResult()
        if (existingUsersGroup == null) {
            val usersGroup: Group = identityService.newGroup("users")
            usersGroup.name = "Users"
            usersGroup.type = "WORKFLOW"
            identityService.saveGroup(usersGroup)
            println("Создана группа users")
        } else {
            println("Группа users уже существует")
        }
        
        // Добавляем пользователя demo в обе группы
        val adminMembership = identityService.createGroupQuery()
            .groupId("camunda-admin")
            .groupMember("demo")
            .singleResult()
        if (adminMembership == null) {
            identityService.createMembership("demo", "camunda-admin")
            println("Пользователь demo добавлен в группу camunda-admin")
        }
        
        val usersMembership = identityService.createGroupQuery()
            .groupId("users")
            .groupMember("demo")
            .singleResult()
        if (usersMembership == null) {
            identityService.createMembership("demo", "users")
            println("Пользователь demo добавлен в группу users")
        }
        
        // Выводим информацию о всех пользователях и группах
        val allUsers = identityService.createUserQuery().list()
        val allGroups = identityService.createGroupQuery().list()
        
        println("=== Все пользователи в системе ===")
        allUsers.forEach { user ->
            println("Пользователь: ${user.id} (${user.firstName} ${user.lastName})")
            val groups = identityService.createGroupQuery().groupMember(user.id).list()
            println("  Группы: ${groups.map { it.id }}")
        }
        
        println("=== Все группы в системе ===")
        allGroups.forEach { group ->
            println("Группа: ${group.id} (${group.name}) - тип: ${group.type}")
        }
    }
}
