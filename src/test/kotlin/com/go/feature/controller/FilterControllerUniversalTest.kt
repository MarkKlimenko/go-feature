package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriBuilder

class FilterControllerUniversalTest : WebIntegrationTest() {
    @Test
    fun getFiltersTest(): Unit = runBlocking {
        val nsResponse: NamespacesResponse = webTestClient.get()
            .uri("/api/v1/namespaces")
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespacesResponse::class.java)
            .responseBody
            .awaitSingle()

        val namespaceResponse: NamespaceResponse = nsResponse.namespaces
            .find { it.name == "filter-test" }
            ?: Assertions.fail("Namespace was not found")

        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("/api/v1/filters")
                    .queryParam("ns", namespaceResponse.id)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.filters[0].id").isNotEmpty
            .jsonPath("$.filters[0].name").isNotEmpty
            .jsonPath("$.filters[0].status").isNotEmpty
    }

    @Test
    fun getNotFoundFilterTest() {
        webTestClient.get()
            .uri("/api/v1/filters/NOT_FOUND")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Filter not found")
    }
}