package com.markklim.feature.configuration.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "localization")
@Validated
data class LocalizationProperties(
    val settings: Settings,
    val messages: Map<String, String>?,
) {
    data class Settings(
        @field:NotBlank
        val localizationHeader: String,

        @field:NotBlank
        val defaultLocalization: String,

        @field:NotBlank
        val defaultMessage: String,

        val substitutor: SubstitutorSettings,
    )

    data class SubstitutorSettings(
        @field:NotBlank
        val prefix: String,

        @field:NotBlank
        val postfix: String,
    )
}
