package com.markklim.feature.service.namespace

import com.markklim.feature.WebIntegrationTest
import com.markklim.feature.persistence.entity.Namespace
import com.markklim.feature.persistence.repository.NamespaceRepository
import com.markklim.feature.service.index.IndexVersionService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired

class NamespaceServiceTest : WebIntegrationTest() {
    @Autowired
    lateinit var namespaceService: NamespaceService

    @Autowired
    lateinit var namespaceRepository: NamespaceRepository

    @Autowired
    lateinit var indexVersionService: IndexVersionService

    @Test
    fun createDefaultNamespaceTest() {
        runBlocking {
            namespaceService.createDefaultNamespace()
            val namespace: Namespace = namespaceRepository.findByName("default")
                ?: fail("Default namespace is null")
            indexVersionService.find(namespace.id)
                ?: fail("Index version is null")

            // no errors for repeatable method launch
            namespaceService.createDefaultNamespace()
            val namespaceRepeatable: Namespace = namespaceRepository.findByName("default")
                ?: fail("Default namespace is null after second method launch")
            assertEquals(namespace.id, namespaceRepeatable.id)
        }
    }
}