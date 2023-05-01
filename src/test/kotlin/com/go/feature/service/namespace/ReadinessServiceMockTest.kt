package com.go.feature.service.namespace

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.service.ProbeResponse
import com.go.feature.service.index.IndexLoaderService
import com.go.feature.service.status.ReadinessService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

class ReadinessServiceMockTest : WebIntegrationTest() {
    @Autowired
    lateinit var readinessService: ReadinessService

    @MockBean
    lateinit var indexLoaderService: IndexLoaderService

    @Test
    fun checkReadinessForUnreadyIndexTest() {
        whenever(indexLoaderService.isIndexLoaded()).thenReturn(false)

        assertEquals(ProbeResponse.Status.WAITING, readinessService.checkReadiness().status)
    }

    @Test
    fun checkReadinessForReadyIndexTest() {
        whenever(indexLoaderService.isIndexLoaded()).thenReturn(true)

        assertEquals(ProbeResponse.Status.UP, readinessService.checkReadiness().status)
    }
}