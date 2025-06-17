package com.example.productservice.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableRabbit
class RabbitConfig {

    companion object {
        const val PRODUCT_EXCHANGE = "product.events"
        const val PRODUCT_CREATED_QUEUE = "product.created"
        const val PRODUCT_UPDATED_QUEUE = "product.updated"
        const val PRODUCT_DELETED_QUEUE = "product.deleted"
        const val STOCK_UPDATED_QUEUE = "stock.updated"
        const val LOW_STOCK_ALERT_QUEUE = "stock.low_alert"
        
        // Order processing queues - должны соответствовать Order Service
        const val STOCK_EXCHANGE = "stock.exchange"
        const val RESERVE_STOCK_QUEUE = "stock.reservation"
        const val RELEASE_STOCK_QUEUE = "stock.release"
    }

    @Bean
    fun productExchange(): TopicExchange {
        return TopicExchange(PRODUCT_EXCHANGE)
    }

    @Bean
    fun stockExchange(): TopicExchange {
        return TopicExchange(STOCK_EXCHANGE)
    }

    @Bean
    fun productCreatedQueue(): Queue {
        return Queue(PRODUCT_CREATED_QUEUE, true)
    }

    @Bean
    fun productUpdatedQueue(): Queue {
        return Queue(PRODUCT_UPDATED_QUEUE, true)
    }

    @Bean
    fun stockUpdatedQueue(): Queue {
        return Queue(STOCK_UPDATED_QUEUE, true)
    }

    @Bean
    fun lowStockAlertQueue(): Queue {
        return Queue(LOW_STOCK_ALERT_QUEUE, true)
    }

    @Bean
    fun reserveStockQueue(): Queue {
        return QueueBuilder.durable(RESERVE_STOCK_QUEUE).build()
    }

    @Bean
    fun releaseStockQueue(): Queue {
        return QueueBuilder.durable(RELEASE_STOCK_QUEUE).build()
    }

    @Bean
    fun productCreatedBinding(): Binding {
        return BindingBuilder.bind(productCreatedQueue()).to(productExchange()).with("product.created")
    }

    @Bean
    fun productUpdatedBinding(): Binding {
        return BindingBuilder.bind(productUpdatedQueue()).to(productExchange()).with("product.updated")
    }

    @Bean
    fun stockUpdatedBinding(): Binding {
        return BindingBuilder.bind(stockUpdatedQueue()).to(productExchange()).with("stock.updated")
    }

    @Bean
    fun lowStockAlertBinding(): Binding {
        return BindingBuilder.bind(lowStockAlertQueue()).to(productExchange()).with("stock.low_alert")
    }

    @Bean
    fun reserveStockBinding(): Binding {
        return BindingBuilder.bind(reserveStockQueue()).to(stockExchange()).with("stock.reserve")
    }

    @Bean
    fun releaseStockBinding(): Binding {
        return BindingBuilder.bind(releaseStockQueue()).to(stockExchange()).with("stock.release")
    }

    @Bean
    fun jackson2MessageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = jackson2MessageConverter()
        return template
    }

    @Bean
    fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setMessageConverter(jackson2MessageConverter())
        return factory
    }
}
