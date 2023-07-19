package com.go.feature.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "localization")
@ConstructorBinding
@Validated
data class LocalizationProperties(
    val settings: Settings = Settings(),
    val messages: Map<String, String>?,
) {
    data class Settings(
        @NotBlank
        val localizationHeader: String = "Accept-Language",
        val defaultLocalization: String = "en",
        val defaultMessage: String = "No message",
        val substitutor: SubstitutorSettings = SubstitutorSettings()
    )

    data class SubstitutorSettings(
        @NotBlank
        val prefix: String = "{",

        @NotBlank
        val postfix: String = "}"
    )
}
