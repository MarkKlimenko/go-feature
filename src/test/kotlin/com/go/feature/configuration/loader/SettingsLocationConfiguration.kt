package com.go.feature.configuration.loader

import com.go.feature.configuration.properties.ApplicationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SettingsLocationConfiguration {

    @Bean
    fun settingsLocation(properties: ApplicationProperties): String =
        this.javaClass.getResource(properties.loader.location)?.toURI()?.path
            ?: throw IllegalArgumentException("test resource not found")
}