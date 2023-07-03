package com.go.feature.configuration.loader

import com.go.feature.configuration.properties.ApplicationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SettingsLocationConfiguration {

    @Bean
    fun settingsLocation(properties: ApplicationProperties): String =
        if (properties.loader.type == ApplicationProperties.LoaderType.DIRECTORY) {
            this.javaClass.getResource(properties.loader.location)?.toURI()?.path
                ?: throw IllegalArgumentException("test resource not found")
        } else {
            properties.loader.location
        }
}