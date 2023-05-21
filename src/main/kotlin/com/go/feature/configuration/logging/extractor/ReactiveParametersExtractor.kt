package com.go.feature.configuration.logging.extractor

import com.go.feature.configuration.logging.properties.LoggingProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.LinkedCaseInsensitiveMap
import org.springframework.util.MultiValueMap

class ReactiveParametersExtractor : RequestParametersExtractor<ServerHttpRequest> {

    override fun extractAllHeaders(request: ServerHttpRequest): Map<String, String> {
        return request.headers.mapValues { extractStringFromList(it.value) }
    }

    override fun extractAllQueries(request: ServerHttpRequest): Map<String, String> {
        return request.queryParams.mapValues { extractStringFromList(it.value) }
    }

    override fun extractSpecificHeaders(
        request: ServerHttpRequest,
        loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
        excludedEntityNames: List<String>
    ): Map<String, String> {
        return if (loggedEntities.isEmpty()) {
            this.extractAllHeaders(request)
                .filterKeys { key -> excludedEntityNames.none { excludedName -> excludedName.equals(key, true) } }
        } else {
            this.extractSpecific(request.headers, filterLoggedEntities(loggedEntities, excludedEntityNames))
        }
    }

    override fun extractSpecificQueries(
        request: ServerHttpRequest,
        loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
    ): Map<String, String> {
        return this.extractSpecific(request.queryParams, loggedEntities)
    }

    private fun filterLoggedEntities(
        loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
        excludedEntityNames: List<String>
    ): List<LoggingProperties.HttpLoggingConfig.LoggedEntity> {
        return loggedEntities
            .filter { loggedEntity ->
                excludedEntityNames.none { excludedName ->
                    excludedName.equals(
                        loggedEntity.actualName,
                        true
                    )
                }
            }
    }

    private fun extractAll(attributes: MultiValueMap<String, String>): Map<String, String> {
        return attributes
            .mapValuesTo(LinkedCaseInsensitiveMap()) { entity ->
                extractStringFromList(entity.value)
            }
    }

    private fun extractStringFromList(strings: List<String>): String {
        return if (strings.size > 1) {
            strings.joinToString(separator = ",")
        } else {
            strings.firstOrNull().orEmpty()
        }
    }

    private fun extractSpecific(
        attributes: MultiValueMap<String, String>,
        entities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
    ): Map<String, String> {
        val logFieldsMap = mutableMapOf<String, String>()
        if (attributes is HttpHeaders) {
            entities.forEach {
                val displayedName = it.displayedName ?: it.actualName.orEmpty()

                attributes[it.actualName.orEmpty()]
                    ?.let { headerValues -> logFieldsMap += displayedName to extractStringFromList(headerValues) }
            }
        } else {
            val extractedAttributes = extractAll(attributes)
            entities.forEach {
                val displayedName = it.displayedName ?: it.actualName.orEmpty()

                extractedAttributes[it.actualName.orEmpty()]
                    ?.let { headerValue -> logFieldsMap += displayedName to headerValue }
            }
        }

        return logFieldsMap
    }
}