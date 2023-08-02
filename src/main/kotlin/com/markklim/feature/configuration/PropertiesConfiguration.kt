package com.markklim.feature.configuration

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.configuration.properties.LocalizationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ApplicationProperties::class,
    LocalizationProperties::class,
)
class PropertiesConfiguration