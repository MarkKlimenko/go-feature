package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class FilterControllerStorageDisabledTest : WebIntegrationTest() {

    @Test
    fun createNamespaceNotAllowedTest(): Unit = runBlocking {
        val request = FilterCreateRequest(
            name = "filterName",
            status = FilterStatus.ENABLED,
            namespace = getNamespace("filter-test").id,
            parameter = "testParameter",
            operator = FilterOperator.EQ,
            description = "filter description",
        )

        webTestClient.post()
            .uri("/api/v1/filters")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("$.message")
            .value(Matchers.containsString("Operation not supported, storage disabled"))
    }

    @Test
    fun editNamespaceNotAllowedTest(): Unit = runBlocking {
        val request = FilterCreateRequest(
            name = "filterName",
            status = FilterStatus.ENABLED,
            namespace = getNamespace("filter-test").id,
            parameter = "testParameter",
            operator = FilterOperator.EQ,
            description = "filter description",
        )

        webTestClient.post()
            .uri("/api/v1/filters/NOT_FOUND")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("$.message")
            .value(Matchers.containsString("Operation not supported, storage disabled"))
    }

    // TODO: use common method
    private suspend fun getNamespace(namespaceName: String): NamespaceResponse {
        val nsResponse: NamespacesResponse = webTestClient.get()
            .uri("/api/v1/namespaces")
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespacesResponse::class.java)
            .responseBody
            .awaitSingle()

        return nsResponse.namespaces
            .find { it.name == namespaceName }
            ?: Assertions.fail("Namespace was not found")
    }
}