package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.feature.FeatureCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.dto.status.Status
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class FeatureControllerStorageDisabledTest : WebIntegrationTest() {

    @Test
    fun createFeatureNotAllowedTest(): Unit = runBlocking {
        // TODO: use common method
        val request = FeatureCreateRequest(
            name = "featureName",
            status = Status.ENABLED,
            namespace = getNamespace("feature-test").id,
            filters = emptyList(),
            description = "feature description",
        )

        webTestClient.post()
            .uri("/api/v1/features")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("$.message")
            .value(Matchers.containsString("Operation not supported, storage disabled"))
    }

    @Test
    fun editFeatureNotAllowedTest(): Unit = runBlocking {
        val request = FeatureCreateRequest(
            name = "featureName",
            status = Status.ENABLED,
            namespace = getNamespace("feature-test").id,
            filters = emptyList(),
            description = "feature description",
        )

        webTestClient.post()
            .uri("/api/v1/features/ANY")
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