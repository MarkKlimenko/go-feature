package com.go.feature.configuration.logging.formatter

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JsonFormatter(private val jacksonObjectMapper: ObjectMapper) {
    private val jacksonPrettyWriter = jacksonObjectMapper.writerWithDefaultPrettyPrinter()

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun toPrettyJsonString(uglyJsonString: String?): String {
        return if (uglyJsonString.isNullOrBlank()) {
            ""
        } else {
            return try {
                jacksonPrettyWriter.writeValueAsString(jacksonObjectMapper.readTree(uglyJsonString))
            } catch (ex: Exception) {
                log.debug("Got an exception when tried to turn into pretty json", ex)
                uglyJsonString
            }
        }
    }

    fun toPrettyJsonBody(body: Any): String? = jacksonPrettyWriter.writeValueAsString(body)
}