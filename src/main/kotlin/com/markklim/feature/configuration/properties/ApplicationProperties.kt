package com.markklim.feature.configuration.properties

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "application")
@Validated
data class ApplicationProperties(
    val namespace: Namespace,
    val filter: Filter,
    val feature: Feature,
    val storage: Storage,
    val loader: Loader
) {
    data class Namespace(
        @field:NotBlank
        val default: String
    )

    data class Filter(
        @field:Min(1)
        @field:Max(100)
        val maxSize: Int
    )

    data class Feature(
        @field:Min(1)
        @field:Max(1000)
        val maxSize: Int
    )

    data class Storage(
        val enabled: Boolean
    )

    data class Loader(
        val type: LoaderType,
        val forceUpdate: Boolean,
        val location: String,
        val git: GitLoader
    )

    data class GitLoader(
        val uri: String?,
        val localDirectory: String,
        val branch: String,
    )

    enum class LoaderType {
        DISABLED, DIRECTORY, GIT
    }
}