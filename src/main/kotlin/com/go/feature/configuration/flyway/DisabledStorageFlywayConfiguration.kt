package com.go.feature.configuration.flyway

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(
    prefix = "application.storage", value = ["enabled"], havingValue = "false", matchIfMissing = true
)
class DisabledStorageFlywayConfiguration {

    @Bean
    @FlywayDataSource
    fun flywayDataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(URL_VALUE)
            .username(USER_VALUE)
            .build()
    }

    private companion object {
        const val URL_VALUE = "jdbc:h2:mem:~/db/internal;DB_CLOSE_DELAY=-1"
        const val USER_VALUE = "sa"
    }
}