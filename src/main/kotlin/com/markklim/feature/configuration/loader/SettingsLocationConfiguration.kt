package com.markklim.feature.configuration.loader

import com.markklim.feature.configuration.properties.ApplicationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SettingsLocationConfiguration {

    @Bean
    fun settingsLocation(properties: ApplicationProperties): String =
        properties.loader.location
}