package com.example.productcatalogservice.config

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
            
            // Развертывание product-process.bpmn
            val productProcessResource = ClassPathResource("product-process.bpmn")
            if (productProcessResource.exists()) {
                repositoryService.createDeployment()
                    .addInputStream("product-process.bpmn", productProcessResource.inputStream)
                    .name("Product Process Deployment")
                    .deploy()
                logger.info("✓ Deployed product-process.bpmn")
            } else {
                logger.warn("✗ product-process.bpmn not found")
            }
            
            // Развертывание product-maintenance.bpmn
            val productMaintenanceResource = ClassPathResource("product-maintenance.bpmn")
            if (productMaintenanceResource.exists()) {
                repositoryService.createDeployment()
                    .addInputStream("product-maintenance.bpmn", productMaintenanceResource.inputStream)
                    .name("Product Maintenance Deployment")
                    .deploy()
                logger.info("✓ Deployed product-maintenance.bpmn")
            } else {
                logger.warn("✗ product-maintenance.bpmn not found")
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
