package com.example.productservice.workflow

import com.example.productservice.service.ProductService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component
class CheckLowStockDelegate(
    private val productService: ProductService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        productService.checkLowStockProducts()
    }
}
