package com.go.feature.configuration.persistence

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.USER
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    prefix = "application.storage", value = ["enabled"], havingValue = "false", matchIfMissing = true
)
class DisabledStorageConfiguration {

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val options: ConnectionFactoryOptions = ConnectionFactoryOptions.builder()
            .from(ConnectionFactoryOptions.parse(URL_VALUE))
            .option(USER, USER_VALUE)
            .build()

        return ConnectionFactories.get(options)
    }

    private companion object {
        const val URL_VALUE = "r2dbc:h2:mem:///~/db/internal"
        const val USER_VALUE = "sa"
    }
}