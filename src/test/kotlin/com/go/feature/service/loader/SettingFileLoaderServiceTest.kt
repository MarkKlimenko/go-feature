package com.go.feature.service.loader

import com.go.feature.WebIntegrationTest
import com.go.feature.test.utils.setting.SettingsProviderUtil
import com.go.feature.util.exception.ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
class SettingFileLoaderServiceTest : WebIntegrationTest() {
    val settingsProviderUtil = SettingsProviderUtil()

    @Autowired
    lateinit var settingFileLoaderService: SettingFileLoaderService

    @Test
    fun loadSettingsWithMaxPossibleFiltersSize(output: CapturedOutput) {
        val settings: ByteArray = settingsProviderUtil.generateSettings(
            "loadSettingsProperFiltersSize",
            19,
            99
        )

        runBlocking {
            settingFileLoaderService.loadSettingFile(settings)
        }

        assertTrue(
            output.out.contains("SETTINGS_LOADER: Finish settings loading for namespace loadSettingsProperFiltersSize")
        )
    }

    @Test
    fun loadSettingsWrongFiltersSize() {
        val settings: ByteArray = settingsProviderUtil.generateSettings(
            "loadSettingsWrongFiltersSize",
            25,
            99
        )

        runBlocking {
            val e: ValidationException = assertThrows {
                settingFileLoaderService.loadSettingFile(settings)
            }

            assertEquals("Enabled filters size exceeds 20", e.message)
        }
    }

    @Test
    fun loadSettingsWrongFeaturesSize() {
        val settings: ByteArray = settingsProviderUtil.generateSettings(
            "loadSettingsWrongFeaturesSize",
            19,
            150
        )

        runBlocking {
            val e: ValidationException = assertThrows {
                settingFileLoaderService.loadSettingFile(settings)
            }

            assertEquals("Enabled features size exceeds 100", e.message)
        }
    }
}