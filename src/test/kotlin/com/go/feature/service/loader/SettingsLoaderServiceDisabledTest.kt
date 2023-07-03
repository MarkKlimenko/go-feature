package com.go.feature.service.loader

import com.go.feature.WebIntegrationTest
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.loader.settings.SettingsLoaderService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.TestPropertySource

@ExtendWith(OutputCaptureExtension::class)
@TestPropertySource(properties = [
    "spring.config.location = classpath:application-disabled-loader.yml"
])
class SettingsLoaderServiceDisabledTest : WebIntegrationTest() {
    @Autowired
    lateinit var settingsLoaderService: SettingsLoaderService

    @Autowired
    lateinit var namespaceRepository: NamespaceRepository

    @Test
    fun loadSettingsFromGitTest(output: CapturedOutput) {
        // no errors for repeatable method launch
        runBlocking {
            settingsLoaderService.loadSettings()
            settingsLoaderService.loadSettings()
        }

        // check default namespace existence
        runBlocking {
            namespaceRepository.findByName("default")
                ?: Assertions.fail("Namespace was not found")
        }

        assertFalse(
            output.out.contains("Error", true)
        )
    }
}