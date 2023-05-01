package com.go.feature.service.namespace

import com.go.feature.WebIntegrationTest
import com.go.feature.service.loader.SettingsLoaderService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SettingsLoaderServiceTest : WebIntegrationTest() {
    @Autowired
    lateinit var settingsLoaderService: SettingsLoaderService

    @Test
    fun loadSettingsTest() {
        // no errors for repeatable method launch
        runBlocking {
            settingsLoaderService.loadSettings()
            settingsLoaderService.loadSettings()
        }
    }
}