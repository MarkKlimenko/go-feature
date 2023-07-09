package com.go.feature.service.loader

import com.go.feature.WebIntegrationTest
import com.go.feature.component.content.provider.ContentProvider
import com.go.feature.service.loader.settings.AtomicSettingsLoader
import com.go.feature.service.loader.settings.SettingsLoaderService
import com.go.feature.test.utils.assertContains
import kotlinx.coroutines.runBlocking
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
    lateinit var fileLoaderService: AtomicSettingsLoader

    @Autowired
    lateinit var settingsLocation: String

    @Autowired
    lateinit var settingsContentProvider: ContentProvider

    @Test
    fun loadSettingsTest(output: CapturedOutput) {
        // no errors for repeatable method launch
        runBlocking {
            settingsLoaderService.loadSettings()
            settingsLoaderService.loadSettings()
        }

        // check error for wrong filter
        output.assertContains("Error: No filter with name=androidVersionMore")
        output.assertContains(
            "Cannot deserialize value of type `com.go.feature.dto.operator.FilterOperator` " +
                "from String \"NEW_OPERATOR\""
        )
    }

    @Test
    fun notFoundSettingsLocation(output: CapturedOutput) {
        val settingsLoaderService = SettingsLoaderService(
            fileLoaderService,
            "$settingsLocation/not_found",
            settingsContentProvider
        )

        runBlocking {
            settingsLoaderService.loadSettings()
        }

        output.assertContains("Settings location not found")
    }

    @Test
    fun emptySettingsLocation(output: CapturedOutput) {
        val settingsLoaderService = SettingsLoaderService(
            fileLoaderService,
            "$settingsLocation/empty",
            settingsContentProvider
        )

        runBlocking {
            settingsLoaderService.loadSettings()
        }

        output.assertContains("SETTINGS_LOADER: Settings location is empty")
    }
}