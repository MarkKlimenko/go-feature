package com.go.feature.service.loader

import com.go.feature.WebIntegrationTest
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

    @Test
    fun loadSettingsTest(output: CapturedOutput) {
        // no errors for repeatable method launch
        runBlocking {
            settingsLoaderService.loadSettings()
            settingsLoaderService.loadSettings()
        }

        // check error for wrong filter
        assertTrue(output.out.contains("Error: No filter with name=androidVersionMore"))
        assertTrue(output.out.contains(
            "Cannot deserialize value of type `com.go.feature.dto.operator.FilterOperator` " +
                "from String \"NEW_OPERATOR\""
        ))
    }
}