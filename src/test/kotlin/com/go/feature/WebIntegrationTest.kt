package com.go.feature

import com.github.tomakehurst.wiremock.WireMockServer
import com.go.feature.service.index.IndexLoaderService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.reactive.server.WebTestClient

// TODO: check trace id
// .header("X-B3-TraceId", "d61436368bae3c12")
// .header("X-B3-SpanId", "ce5f844337f3ee88")

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