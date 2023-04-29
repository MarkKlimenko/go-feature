package com.go.feature.configuration.loader

import com.go.feature.configuration.properties.ApplicationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SettingsLoaderConfiguration {

    @Bean
    fun settingsLocation(properties: ApplicationProperties): String =
        properties.loader.location
}