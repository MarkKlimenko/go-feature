package com.go.feature.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "application")
@ConstructorBinding
@Validated
data class ApplicationProperties(
    val namespace: Namespace
) {
    data class Namespace(
        @field:NotBlank
        val default: String
    )
}