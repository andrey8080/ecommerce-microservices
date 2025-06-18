package com.example.paymentservice.config

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
            
            // Развертывание payment-process.bpmn
            val paymentProcessResource = ClassPathResource("payment-process.bpmn")
            if (paymentProcessResource.exists()) {
                repositoryService.createDeployment()
                    .addInputStream("payment-process.bpmn", paymentProcessResource.inputStream)
                    .name("Payment Process Deployment")
                    .deploy()
                logger.info("✓ Deployed payment-process.bpmn")
            } else {
                logger.warn("✗ payment-process.bpmn not found")
            }
            
            // Развертывание payment-maintenance.bpmn
            val paymentMaintenanceResource = ClassPathResource("payment-maintenance.bpmn")
            if (paymentMaintenanceResource.exists()) {
                repositoryService.createDeployment()
                    .addInputStream("payment-maintenance.bpmn", paymentMaintenanceResource.inputStream)
                    .name("Payment Maintenance Deployment")
                    .deploy()
                logger.info("✓ Deployed payment-maintenance.bpmn")
            } else {
                logger.warn("✗ payment-maintenance.bpmn not found")
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
