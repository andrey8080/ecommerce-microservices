package com.example.userservice.config

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
        const val USER_EXCHANGE = "user.events"
        const val USER_CREATED_QUEUE = "user.created"
        const val USER_UPDATED_QUEUE = "user.updated"
        const val USER_DELETED_QUEUE = "user.deleted"
        const val USER_LOGIN_QUEUE = "user.login"
    }

    @Bean
    fun userExchange(): TopicExchange {
        return TopicExchange(USER_EXCHANGE)
    }

    @Bean
    fun userCreatedQueue(): Queue {
        return Queue(USER_CREATED_QUEUE, true)
    }

    @Bean
    fun userUpdatedQueue(): Queue {
        return Queue(USER_UPDATED_QUEUE, true)
    }

    @Bean
    fun userDeletedQueue(): Queue {
        return Queue(USER_DELETED_QUEUE, true)
    }

    @Bean
    fun userLoginQueue(): Queue {
        return Queue(USER_LOGIN_QUEUE, true)
    }

    @Bean
    fun userCreatedBinding(): Binding {
        return BindingBuilder.bind(userCreatedQueue()).to(userExchange()).with("user.created")
    }

    @Bean
    fun userUpdatedBinding(): Binding {
        return BindingBuilder.bind(userUpdatedQueue()).to(userExchange()).with("user.updated")
    }

    @Bean
    fun userDeletedBinding(): Binding {
        return BindingBuilder.bind(userDeletedQueue()).to(userExchange()).with("user.deleted")
    }

    @Bean
    fun userLoginBinding(): Binding {
        return BindingBuilder.bind(userLoginQueue()).to(userExchange()).with("user.login")
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
