package com.markklim.feature.service.loader

import com.markklim.feature.WebIntegrationTest
import com.markklim.feature.persistence.repository.NamespaceRepository
import com.markklim.feature.service.loader.settings.SettingsLoaderService
import com.markklim.feature.test.utils.assertNotContains
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.TestPropertySource
import java.io.File

@ExtendWith(OutputCaptureExtension::class)
@TestPropertySource(properties = [
    "spring.config.location = classpath:application-git-loader.yml"
])
class SettingsLoaderServiceGitTest : WebIntegrationTest() {
    @Autowired
    lateinit var settingsLoaderService: SettingsLoaderService

    @Autowired
    lateinit var namespaceRepository: NamespaceRepository

    @Test
    fun loadSettingsFromGitTest(output: CapturedOutput) {
        File("tmp/setting").deleteRecursively()

        // no errors for repeatable method launch
        runBlocking {
            settingsLoaderService.loadSettings()
            settingsLoaderService.loadSettings()
        }

        // check new namespace existence
        runBlocking {
            namespaceRepository.findByName("from-git")
                ?: Assertions.fail("Namespace was not found")
        }

        output.assertNotContains("Error")
    }
}