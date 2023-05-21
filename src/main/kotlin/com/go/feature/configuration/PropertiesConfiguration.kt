package com.go.feature.configuration

import com.go.feature.configuration.logging.properties.LoggingProperties
import com.go.feature.configuration.properties.ApplicationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ApplicationProperties::class,
    LoggingProperties::class
)
class PropertiesConfiguration