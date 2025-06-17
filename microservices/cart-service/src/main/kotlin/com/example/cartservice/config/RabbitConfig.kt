package com.example.cartservice.config

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
        // Exchange
        const val CART_EXCHANGE = "cart.exchange"
        const val ORDER_EXCHANGE = "order.exchange"
        
        // Queues
        const val CART_EVENTS_QUEUE = "cart.events"
        const val CART_ABANDONED_QUEUE = "cart.abandoned"
        const val ORDER_CREATED_QUEUE = "order.created"
        
        // Routing keys
        const val CART_ITEM_ADDED = "cart.item.added"
        const val CART_ITEM_REMOVED = "cart.item.removed" 
        const val CART_ITEM_UPDATED = "cart.item.updated"
        const val CART_CLEARED = "cart.cleared"
        const val CART_ABANDONED = "cart.abandoned"
        const val ORDER_CREATED = "order.created"
    }

    @Bean
    fun cartExchange(): TopicExchange {
        return TopicExchange(CART_EXCHANGE)
    }
    
    @Bean
    fun orderExchange(): TopicExchange {
        return TopicExchange(ORDER_EXCHANGE)
    }

    @Bean
    fun cartEventsQueue(): Queue {
        return QueueBuilder.durable(CART_EVENTS_QUEUE).build()
    }
    
    @Bean
    fun cartAbandonedQueue(): Queue {
        return QueueBuilder.durable(CART_ABANDONED_QUEUE).build()
    }
    
    @Bean
    fun orderCreatedQueue(): Queue {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build()
    }

    @Bean
    fun cartEventsBinding(): Binding {
        return BindingBuilder
            .bind(cartEventsQueue())
            .to(cartExchange())
            .with("cart.*")
    }
    
    @Bean
    fun cartAbandonedBinding(): Binding {
        return BindingBuilder
            .bind(cartAbandonedQueue())
            .to(cartExchange())
            .with(CART_ABANDONED)
    }
    
    @Bean
    fun orderCreatedBinding(): Binding {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(orderExchange())
            .with(ORDER_CREATED)
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
