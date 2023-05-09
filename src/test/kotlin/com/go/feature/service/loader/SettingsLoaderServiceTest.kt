package com.go.feature.service.loader

import com.go.feature.WebIntegrationTest
import com.go.feature.configuration.properties.ApplicationProperties
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
class SettingsLoaderServiceTest : WebIntegrationTest() {
    @Autowired
    lateinit var settingsLoaderService: SettingsLoaderService

    @Autowired
    lateinit var applicationProperties: ApplicationProperties

    @Autowired
    lateinit var fileLoaderService: SettingFileLoaderService

    @Autowired
    lateinit var settingsLocation: String

    @Test
    fun loadSettingsTest(output: CapturedOutput) {
        // no errors for repeatable method launch
        runBlocking {
            settingsLoaderService.loadSettings()
            settingsLoaderService.loadSettings()
        }

        // check error for wrong filter
        assertTrue(output.out.contains("Error: No filter with name=androidVersionMore"))
        assertTrue(
            output.out.contains(
                "Cannot deserialize value of type `com.go.feature.dto.operator.FilterOperator` " +
                    "from String \"NEW_OPERATOR\""
            )
        )
    }

    @Test
    fun notFoundSettingsLocation(output: CapturedOutput) {
        val settingsLoaderService = SettingsLoaderService(
            applicationProperties,
            fileLoaderService,
            "$settingsLocation/not_found"
        )

        runBlocking {
            settingsLoaderService.loadSettings()
        }

        assertTrue(
            output.out.contains("SETTINGS_LOADER: Settings location not found")
        )
    }

    @Test
    fun emptySettingsLocation(output: CapturedOutput) {
        val settingsLoaderService = SettingsLoaderService(
            applicationProperties,
            fileLoaderService,
            "$settingsLocation/empty"
        )

        runBlocking {
            settingsLoaderService.loadSettings()
        }

        assertTrue(
            output.out.contains("SETTINGS_LOADER: Settings location is empty")
        )
    }
}