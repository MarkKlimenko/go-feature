package com.go.feature.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.service.dto.loader.LoadedSettings
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class SettingsLoaderService(
    val applicationProperties: ApplicationProperties,
    val objectMapper: ObjectMapper,
) {

    //TODO: load all directory
    suspend fun loadSettings() {
        val settings: LoadedSettings =
            objectMapper.readValue(Files.readAllBytes(Paths.get(applicationProperties.loader.location)))

        //TODO: implement
    }
}