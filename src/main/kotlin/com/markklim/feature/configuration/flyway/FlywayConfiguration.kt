package com.markklim.feature.configuration.flyway

import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfiguration {
    @Bean
    fun flywayConfigurationCustomizer(postMigrationCallback: PostMigrationCallback): FlywayConfigurationCustomizer {
        return FlywayConfigurationCustomizer { configuration: FluentConfiguration ->
            configuration.callbacks(postMigrationCallback)
        }
    }
}