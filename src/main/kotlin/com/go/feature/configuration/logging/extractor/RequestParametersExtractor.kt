package com.go.feature.configuration.logging.extractor

import com.go.feature.configuration.logging.properties.LoggingProperties


interface RequestParametersExtractor<T> {

    fun extractAllHeaders(request: T): Map<String, String>
    fun extractAllQueries(request: T): Map<String, String>
    fun extractSpecificHeaders(request: T,
                               loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
                               excludedEntityNames: List<String>): Map<String, String>
    fun extractSpecificQueries(request: T,
                               loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>): Map<String, String>
}