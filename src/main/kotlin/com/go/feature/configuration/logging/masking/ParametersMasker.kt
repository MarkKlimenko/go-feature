package com.go.feature.configuration.logging.masking

import com.go.feature.configuration.logging.properties.LoggingProperties


class ParametersMasker {

    fun maskParametersServlet(params: Map<String, String>,
                       rules: List<LoggingProperties.HttpLoggingConfig.MaskedEntity>): Map<String, String> {
        return maskParameters(params, rules).mapValues { it.value.toString() }
    }

    fun maskParametersReactive(params: Map<String, Any>,
                       rules: List<LoggingProperties.HttpLoggingConfig.MaskedEntity>): Map<String, Any> {
        return maskParameters(params, rules)
    }

    private fun maskParameters(params: Map<String, Any>,
                              rules: List<LoggingProperties.HttpLoggingConfig.MaskedEntity>): Map<String, Any> {
        return params.mapValues {

            rules.firstOrNull { rule -> rule.displayedName.equals(it.key, true) }
            ?.let {rule ->  rule.sensitiveDataPattern
                ?.matcher(it.value.toString())
                ?.replaceAll(rule.substitutionValue)
                ?.toString()}
            ?: it.value
        }
    }
}