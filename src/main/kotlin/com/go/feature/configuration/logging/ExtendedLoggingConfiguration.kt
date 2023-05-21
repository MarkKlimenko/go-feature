package com.go.feature.configuration.logging

import com.go.feature.configuration.logging.extractor.ReactiveParametersExtractor
import com.go.feature.configuration.logging.masking.ParametersMasker
import com.go.feature.configuration.logging.properties.LoggingProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer

@Configuration
class ExtendedLoggingConfiguration {
    //@Bean
    //@ConditionalOnMissingBean(ServerCodecConfigurer::class)
    //fun serverCodecConfigurer() = ServerCodecConfigurer.create()

    @Bean
    fun paramsExtractor() = ReactiveParametersExtractor()

    @Bean
    @ConditionalOnMissingBean(ParametersMasker::class)
    fun parametersMasker() = ParametersMasker()

    @Bean
    fun loggingFilter(
        loggingProperties: LoggingProperties,
        serverCodecConfigurer: ServerCodecConfigurer
    ) = LoggingFilter(
        loggingProperties,
        paramsExtractor(),
        parametersMasker(),
        serverCodecConfigurer
    )
}