package com.go.feature.configuration.loader

import com.go.feature.component.content.provider.ContentProvider
import com.go.feature.component.content.provider.FileContentProvider
import com.go.feature.component.content.provider.GitContentProvider
import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.configuration.properties.ApplicationProperties.LoaderType.DIRECTORY
import com.go.feature.configuration.properties.ApplicationProperties.LoaderType.GIT
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SettingsLoaderConfiguration {

    @Bean
    fun settingsContentProvider(properties: ApplicationProperties): ContentProvider =
        when (properties.loader.type) {
            DIRECTORY -> FileContentProvider()
            GIT -> GitContentProvider()
        }
}