package com.markklim.feature.configuration.loader

import com.markklim.feature.component.content.provider.GitContentProvider
import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.configuration.properties.ApplicationProperties.LoaderType.DIRECTORY
import com.markklim.feature.configuration.properties.ApplicationProperties.LoaderType.DISABLED
import com.markklim.feature.configuration.properties.ApplicationProperties.LoaderType.GIT
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SettingsLoaderConfiguration {

    @Bean
    fun settingsContentProvider(properties: ApplicationProperties): com.markklim.feature.component.content.provider.ContentProvider =
        when (properties.loader.type) {
            DIRECTORY -> com.markklim.feature.component.content.provider.FileContentProvider()

            GIT -> {
                val uri: String? = properties.loader.git.uri
                if (uri.isNullOrBlank()) {
                    throw IllegalArgumentException("Git Content Provider - required parameter 'uri' is blank or null")
                }

                GitContentProvider(
                    uri = uri,
                    localDirectory = properties.loader.git.localDirectory,
                    branch = properties.loader.git.branch,
                )
            }

            DISABLED -> com.markklim.feature.component.content.provider.EmptyContentProvider()
        }
}