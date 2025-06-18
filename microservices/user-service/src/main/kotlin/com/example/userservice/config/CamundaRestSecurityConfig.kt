package com.example.userservice.config

import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CamundaRestSecurityConfig {

    @Bean
    fun processEngineAuthenticationFilter(): FilterRegistrationBean<ProcessEngineAuthenticationFilter> {
        val filterRegistration = FilterRegistrationBean<ProcessEngineAuthenticationFilter>()
        filterRegistration.filter = ProcessEngineAuthenticationFilter()
        filterRegistration.addInitParameter("authentication-provider", "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider")
        filterRegistration.addUrlPatterns("/camunda/api/*")
        filterRegistration.order = 200
        return filterRegistration
    }
}
