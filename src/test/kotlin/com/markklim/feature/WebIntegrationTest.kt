package com.markklim.feature

import com.github.tomakehurst.wiremock.WireMockServer
import com.markklim.feature.service.index.IndexLoaderService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureWebTestClient
class WebIntegrationTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired(required = false)
    lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var indexLoaderService: IndexLoaderService

    @BeforeEach
    fun init() {
        wireMockServer.resetAll()

        runBlocking {
            indexLoaderService.loadIndexes()
        }
    }
}