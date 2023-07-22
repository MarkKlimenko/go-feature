package com.go.feature.service.loader

import com.go.feature.WebIntegrationTest
import com.go.feature.service.loader.settings.AtomicSettingsLoader
import com.go.feature.test.utils.assertContains
import com.go.feature.test.utils.setting.SettingsProviderUtil
import com.go.feature.util.exception.client.ClientException
import com.go.feature.util.exception.internal.InternalValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@ExtendWith(OutputCaptureExtension::class)
class AtomicSettingLoaderTest : WebIntegrationTest() {
    val settingsProviderUtil = SettingsProviderUtil()

    @Autowired
    lateinit var settingsLoaderAtomicService: AtomicSettingsLoader

    @Test
    fun loadSettingsWithMaxPossibleFiltersSize(output: CapturedOutput): Unit = runBlocking {
        val settings: ByteArray = settingsProviderUtil.generateSettings(
            "loadSettingsProperFiltersSize",
            19,
            99
        )

        settingsLoaderAtomicService.loadSettingFile(settings)

        output.assertContains("SETTINGS_LOADER: Finish settings loading for namespace loadSettingsProperFiltersSize")
    }

    @Test
    fun loadSettingsWrongFiltersSize(): Unit = runBlocking {
        val settings: ByteArray = settingsProviderUtil.generateSettings(
            "loadSettingsWrongFiltersSize",
            25,
            99
        )

        val e: InternalValidationException = assertThrows {
            settingsLoaderAtomicService.loadSettingFile(settings)
        }

        assertEquals("Enabled filters size exceeds 20", e.message)
    }

    @Test
    fun loadSettingsWrongFeaturesSize(): Unit = runBlocking {
        val settings: ByteArray = settingsProviderUtil.generateSettings(
            "loadSettingsWrongFeaturesSize",
            19,
            1001
        )

        val e: InternalValidationException = assertThrows {
            settingsLoaderAtomicService.loadSettingFile(settings)
        }

        assertEquals("Enabled features size exceeds 1000", e.message)
    }
}