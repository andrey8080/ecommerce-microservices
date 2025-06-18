package com.example.orderservice.config

import org.camunda.bpm.engine.ProcessEngine
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class BpmnDeploymentConfig : ApplicationRunner {

    @Autowired
    private lateinit var processEngine: ProcessEngine
    
    private val logger = LoggerFactory.getLogger(BpmnDeploymentConfig::class.java)

    override fun run(args: ApplicationArguments?) {
        logger.info("Starting BPMN process deployment...")
        deployBpmnProcesses()
    }

    private fun deployBpmnProcesses() {
        try {
            val repositoryService = processEngine.repositoryService
            
            // Развертывание order-full-process.bpmn
            val orderProcessResource = ClassPathResource("order-full-process.bpmn")
            if (orderProcessResource.exists()) {
                repositoryService.createDeployment()
                    .addInputStream("order-full-process.bpmn", orderProcessResource.inputStream)
                    .name("Order Process Deployment")
                    .deploy()
                logger.info("✓ Deployed order-full-process.bpmn")
            } else {
                logger.warn("✗ order-full-process.bpmn not found")
            }
            
            // Логирование развернутых процессов
            val deployedProcesses = repositoryService.createProcessDefinitionQuery().list()
            logger.info("=== Deployed Process Definitions ===")
            deployedProcesses.forEach { processDefinition ->
                logger.info("Process: ${processDefinition.key} (${processDefinition.name}) - Version: ${processDefinition.version}")
            }
            
        } catch (e: Exception) {
            logger.error("Error deploying BPMN processes: ${e.message}", e)
        }
    }
}
