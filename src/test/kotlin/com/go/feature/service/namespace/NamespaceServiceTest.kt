package com.go.feature.service.namespace

import com.go.feature.WebIntegrationTest
import com.go.feature.service.NamespaceService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NamespaceServiceTest : WebIntegrationTest() {
    @Autowired
    lateinit var namespaceService: NamespaceService

    @Test
    fun createDefaultNamespaceTest() {
        // no error for repeatable method launch
        runBlocking {
            namespaceService.createDefaultNamespace()
            namespaceService.createDefaultNamespace()
        }
    }
}