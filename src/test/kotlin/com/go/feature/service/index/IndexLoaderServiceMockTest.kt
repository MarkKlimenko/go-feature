package com.go.feature.service.index

import com.go.feature.WebIntegrationTest
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.IndexVersionService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean

class IndexLoaderServiceMockTest : WebIntegrationTest() {
    @Autowired
    lateinit var indexLoaderService: IndexLoaderService

    @Autowired
    lateinit var indexVersionService: IndexVersionService

    @SpyBean
    lateinit var namespaceRepository: NamespaceRepository

    @SpyBean
    lateinit var indexService: IndexService

    @Test
    fun noErrorsThrownForNullNamespace() {
        runBlocking {
            val namespace: Namespace = namespaceRepository.findByName("default")
                ?: fail("Namespace not found")

            whenever(namespaceRepository.findById(namespace.id)).thenReturn(null)

            indexLoaderService.loadIndexes()
        }
    }

    @Test
    fun noErrorsThrownForNullInternalIndexVersion() {
        runBlocking {
            val namespace: Namespace = namespaceRepository.findByName("default")
                ?: fail("Namespace not found")

            whenever(indexService.getInternalIndexVersion(namespace.id)).thenReturn(null)

            indexLoaderService.loadIndexes()
        }
    }

    @Test
    fun noErrorsThrownAfterIndexUpdate() {
        runBlocking {
            // update index for namespace "contains",
            // namespace "default" can not be used because of other mocks
            val namespace: Namespace = namespaceRepository.findByName("contains")
                ?: fail("Namespace not found")

            indexVersionService.update(namespace.id)

            indexLoaderService.loadIndexes()
        }
    }
}