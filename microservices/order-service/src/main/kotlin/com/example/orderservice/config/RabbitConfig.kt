package com.example.orderservice.config

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
        const val ORDER_EXCHANGE = "order.exchange"
        const val STOCK_EXCHANGE = "stock.exchange"
        const val PAYMENT_EXCHANGE = "payment.exchange"
        const val NOTIFICATION_EXCHANGE = "notification.exchange"
        
        // Queues
        const val ORDER_CREATED_QUEUE = "order.created"
        const val ORDER_CONFIRMED_QUEUE = "order.confirmed"
        const val ORDER_CANCELLED_QUEUE = "order.cancelled"
        const val ORDER_COMPLETED_QUEUE = "order.completed"
        const val STOCK_RESERVATION_QUEUE = "stock.reservation"
        const val STOCK_RESERVATION_RESPONSE_QUEUE = "stock.reservation.response"
        const val PAYMENT_PROCESSING_QUEUE = "payment.processing"
        const val NOTIFICATION_EMAIL_QUEUE = "notification.email"
        const val NOTIFICATION_SMS_QUEUE = "notification.sms"
        
        // Routing Keys
        const val ORDER_CREATED = "order.created"
        const val ORDER_CONFIRMED = "order.confirmed"
        const val ORDER_CANCELLED = "order.cancelled"
        const val ORDER_COMPLETED = "order.completed"
        const val STOCK_RESERVE = "stock.reserve"
        const val STOCK_RELEASE = "stock.release"
        const val PAYMENT_PROCESS = "payment.process"
        const val NOTIFICATION_EMAIL = "notification.email"
        const val NOTIFICATION_SMS = "notification.sms"
    }

    @Bean
    fun orderExchange(): TopicExchange {
        return TopicExchange(ORDER_EXCHANGE)
    }
    
    @Bean
    fun stockExchange(): TopicExchange {
        return TopicExchange(STOCK_EXCHANGE)
    }
    
    @Bean
    fun paymentExchange(): TopicExchange {
        return TopicExchange(PAYMENT_EXCHANGE)
    }
    
    @Bean
    fun notificationExchange(): TopicExchange {
        return TopicExchange(NOTIFICATION_EXCHANGE)
    }

    // Order Queues
    @Bean
    fun orderCreatedQueue(): Queue {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build()
    }
    
    @Bean
    fun orderConfirmedQueue(): Queue {
        return QueueBuilder.durable(ORDER_CONFIRMED_QUEUE).build()
    }
    
    @Bean
    fun orderCancelledQueue(): Queue {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE).build()
    }
    
    @Bean
    fun orderCompletedQueue(): Queue {
        return QueueBuilder.durable(ORDER_COMPLETED_QUEUE).build()
    }

    // Stock Queues
    @Bean
    fun stockReservationQueue(): Queue {
        return QueueBuilder.durable(STOCK_RESERVATION_QUEUE).build()
    }
    
    @Bean
    fun stockReservationResponseQueue(): Queue {
        return QueueBuilder.durable(STOCK_RESERVATION_RESPONSE_QUEUE).build()
    }

    // Payment Queues
    @Bean
    fun paymentProcessingQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_PROCESSING_QUEUE).build()
    }

    // Notification Queues
    @Bean
    fun notificationEmailQueue(): Queue {
        return QueueBuilder.durable(NOTIFICATION_EMAIL_QUEUE).build()
    }
    
    @Bean
    fun notificationSmsQueue(): Queue {
        return QueueBuilder.durable(NOTIFICATION_SMS_QUEUE).build()
    }

    // Bindings
    @Bean
    fun orderCreatedBinding(): Binding {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(orderExchange())
            .with(ORDER_CREATED)
    }
    
    @Bean
    fun orderConfirmedBinding(): Binding {
        return BindingBuilder
            .bind(orderConfirmedQueue())
            .to(orderExchange())
            .with(ORDER_CONFIRMED)
    }
    
    @Bean
    fun orderCancelledBinding(): Binding {
        return BindingBuilder
            .bind(orderCancelledQueue())
            .to(orderExchange())
            .with(ORDER_CANCELLED)
    }
    
    @Bean
    fun orderCompletedBinding(): Binding {
        return BindingBuilder
            .bind(orderCompletedQueue())
            .to(orderExchange())
            .with(ORDER_COMPLETED)
    }
    
    @Bean
    fun stockReservationBinding(): Binding {
        return BindingBuilder
            .bind(stockReservationQueue())
            .to(stockExchange())
            .with(STOCK_RESERVE)
    }
    
    @Bean
    fun stockReservationResponseBinding(): Binding {
        return BindingBuilder
            .bind(stockReservationResponseQueue())
            .to(stockExchange())
            .with("stock.reservation.response")
    }
    
    @Bean
    fun paymentProcessingBinding(): Binding {
        return BindingBuilder
            .bind(paymentProcessingQueue())
            .to(paymentExchange())
            .with(PAYMENT_PROCESS)
    }
    
    @Bean
    fun notificationEmailBinding(): Binding {
        return BindingBuilder
            .bind(notificationEmailQueue())
            .to(notificationExchange())
            .with(NOTIFICATION_EMAIL)
    }
    
    @Bean
    fun notificationSmsBinding(): Binding {
        return BindingBuilder
            .bind(notificationSmsQueue())
            .to(notificationExchange())
            .with(NOTIFICATION_SMS)
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
