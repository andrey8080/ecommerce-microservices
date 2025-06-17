pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.20"
        kotlin("plugin.spring") version "2.1.20"
        kotlin("plugin.jpa") version "2.1.20"
        id("org.springframework.boot") version "3.2.5"
        id("io.spring.dependency-management") version "1.1.4"
        id("org.jetbrains.kotlin.jvm") version "2.1.20"
        id("org.jetbrains.kotlin.plugin.spring") version "2.1.20"
        id("org.jetbrains.kotlin.plugin.jpa") version "2.1.20"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "software_systems_business_logic_lab3"

include(":microservices:user-service")
include(":microservices:product-catalog-service")
include(":microservices:cart-service")
include(":microservices:order-service")
include(":microservices:payment-service")
include(":microservices:api-gateway")
