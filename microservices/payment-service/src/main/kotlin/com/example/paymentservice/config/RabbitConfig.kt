package com.example.paymentservice.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableRabbit
class RabbitConfig {

    companion object {
        // Exchanges
        const val PAYMENT_EXCHANGE = "payment.exchange"
        const val ORDER_EXCHANGE = "order.exchange"
        const val NOTIFICATION_EXCHANGE = "notification.exchange"
        
        // Queues
        const val PAYMENT_PROCESSING_QUEUE = "payment.processing"
        const val PAYMENT_COMPLETED_QUEUE = "payment.completed"
        const val PAYMENT_FAILED_QUEUE = "payment.failed"
        const val PAYMENT_REFUNDED_QUEUE = "payment.refunded"
        const val ORDER_PAYMENT_UPDATE_QUEUE = "order.payment.update"
        const val NOTIFICATION_PAYMENT_QUEUE = "notification.payment"
        
        // Routing Keys
        const val PAYMENT_PROCESS = "payment.process"
        const val PAYMENT_COMPLETED = "payment.completed"
        const val PAYMENT_FAILED = "payment.failed"
        const val PAYMENT_REFUNDED = "payment.refunded"
        const val ORDER_PAYMENT_UPDATE = "order.payment.update"
        const val NOTIFICATION_PAYMENT = "notification.payment"
    }

    @Bean
    fun paymentExchange(): TopicExchange {
        return TopicExchange(PAYMENT_EXCHANGE)
    }
    
    @Bean
    fun orderExchange(): TopicExchange {
        return TopicExchange(ORDER_EXCHANGE)
    }
    
    @Bean
    fun notificationExchange(): TopicExchange {
        return TopicExchange(NOTIFICATION_EXCHANGE)
    }

    // Payment Queues
    @Bean
    fun paymentProcessingQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_PROCESSING_QUEUE).build()
    }
    
    @Bean
    fun paymentCompletedQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build()
    }
    
    @Bean
    fun paymentFailedQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build()
    }
    
    @Bean
    fun paymentRefundedQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_REFUNDED_QUEUE).build()
    }
    
    @Bean
    fun orderPaymentUpdateQueue(): Queue {
        return QueueBuilder.durable(ORDER_PAYMENT_UPDATE_QUEUE).build()
    }
    
    @Bean
    fun notificationPaymentQueue(): Queue {
        return QueueBuilder.durable(NOTIFICATION_PAYMENT_QUEUE).build()
    }

    // Bindings
    @Bean
    fun paymentProcessingBinding(): Binding {
        return BindingBuilder
            .bind(paymentProcessingQueue())
            .to(paymentExchange())
            .with(PAYMENT_PROCESS)
    }
    
    @Bean
    fun paymentCompletedBinding(): Binding {
        return BindingBuilder
            .bind(paymentCompletedQueue())
            .to(paymentExchange())
            .with(PAYMENT_COMPLETED)
    }
    
    @Bean
    fun paymentFailedBinding(): Binding {
        return BindingBuilder
            .bind(paymentFailedQueue())
            .to(paymentExchange())
            .with(PAYMENT_FAILED)
    }
    
    @Bean
    fun paymentRefundedBinding(): Binding {
        return BindingBuilder
            .bind(paymentRefundedQueue())
            .to(paymentExchange())
            .with(PAYMENT_REFUNDED)
    }
    
    @Bean
    fun orderPaymentUpdateBinding(): Binding {
        return BindingBuilder
            .bind(orderPaymentUpdateQueue())
            .to(orderExchange())
            .with(ORDER_PAYMENT_UPDATE)
    }
    
    @Bean
    fun notificationPaymentBinding(): Binding {
        return BindingBuilder
            .bind(notificationPaymentQueue())
            .to(notificationExchange())
            .with(NOTIFICATION_PAYMENT)
    }

    @Bean
    fun jsonMessageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = jsonMessageConverter()
        return template
    }
}
