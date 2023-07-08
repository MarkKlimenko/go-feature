package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import org.springframework.web.util.UriBuilder

@TestPropertySource(properties = [
    "spring.config.location = classpath:application-storage-enabled.yml"
])
class FilterControllerStorageEnabledTest : WebIntegrationTest() {
    @Test
    fun deleteFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace()
        val filter: FilterResponse = getFilter(namespace.id, "forDeleteEq")

        webTestClient.delete()
            .uri("/api/v1/filters/${filter.id}")
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/api/v1/filters/${filter.id}")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Filter not found")
    }

    @Test
    fun deleteUsedFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace()
        val filter: FilterResponse = getFilter(namespace.id, "userNameEq")

        webTestClient.delete()
            .uri("/api/v1/filters/${filter.id}")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message")
            .isEqualTo("Filter 'userNameEq' is used at most by one feature 'testFeature'")

        webTestClient.get()
            .uri("/api/v1/filters/${filter.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    private suspend fun getNamespace(): NamespaceResponse {
        val nsResponse: NamespacesResponse = webTestClient.get()
            .uri("/api/v1/namespaces")
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespacesResponse::class.java)
            .responseBody
            .awaitSingle()

        return nsResponse.namespaces
            .find { it.name == "eq" }
            ?: fail("Namespace was not found")
    }

    private suspend fun getFilter(namespaceId: String, filterName: String): FilterResponse {
        val response: FiltersResponse = webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("/api/v1/filters")
                    .queryParam("ns", namespaceId)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .returnResult(FiltersResponse::class.java)
            .responseBody
            .awaitSingle()

        return response.filters
            .find { it.name == filterName }
            ?: fail("Filter was not found")
    }
}