import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task

fun main() {
    val processEngine = ProcessEngines.getDefaultProcessEngine()
    
    println("=== Проверка видимости задач ===")
    
    // Получаем все задачи
    val allTasks = processEngine.taskService.createTaskQuery().list()
    println("Всего задач в системе: ${allTasks.size}")
    
    allTasks.forEach { task ->
        println("Задача: ${task.name} (ID: ${task.id})")
        println("  - Процесс: ${task.processDefinitionId}")
        println("  - Назначена пользователю: ${task.assignee}")
        println("  - Кандидаты пользователи: ${task.candidateUsers}")
        println("  - Кандидаты группы: ${task.candidateGroups}")
        println()
    }
    
    // Получаем задачи для пользователя demo
    val demoTasks = processEngine.taskService.createTaskQuery()
        .taskCandidateUser("demo")
        .list()
    println("Задач для пользователя 'demo': ${demoTasks.size}")
    
    demoTasks.forEach { task ->
        println("Доступная задача для demo: ${task.name} (ID: ${task.id})")
    }
    
    // Получаем все экземпляры процессов
    val processInstances = processEngine.runtimeService.createProcessInstanceQuery().list()
    println("\nВсего активных экземпляров процессов: ${processInstances.size}")
    
    processInstances.forEach { instance ->
        println("Экземпляр: ${instance.processDefinitionId} (ID: ${instance.id})")
    }
    
    // Попробуем запустить новый экземпляр процесса регистрации пользователя
    try {
        val newInstance = processEngine.runtimeService.startProcessInstanceByKey("userRegistrationProcess")
        println("\nЗапущен новый экземпляр процесса: ${newInstance.id}")
        
        // Проверяем задачи после запуска
        val newTasks = processEngine.taskService.createTaskQuery()
            .processInstanceId(newInstance.id)
            .list()
        
        println("Задач в новом экземпляре: ${newTasks.size}")
        newTasks.forEach { task ->
            println("Новая задача: ${task.name} (ID: ${task.id})")
            println("  - Кандидаты: ${task.candidateUsers}")
        }
    } catch (e: Exception) {
        println("Ошибка при запуске процесса: ${e.message}")
    }
}
