package com.markklim.feature.controller

import com.markklim.feature.WebIntegrationTest
import com.markklim.feature.controller.dto.namespace.NamespaceCreateRequest
import com.markklim.feature.controller.dto.namespace.NamespaceEditRequest
import com.markklim.feature.dto.status.Status
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class NamespaceControllerStorageDisabledTest : WebIntegrationTest() {
    @Test
    fun createNamespaceNotAllowedTest() {
        val request = NamespaceCreateRequest(
            name = "newNamespace",
            status = Status.ENABLED
        )

        webTestClient.post()
            .uri("/api/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("$.message")
            .value(Matchers.containsString("Operation not supported, storage disabled"))
    }

    @Test
    fun editNamespaceNotAllowedTest() {
        val request = NamespaceEditRequest(
            name = "editedNamespace",
            status = Status.DISABLED,
            version = 0
        )

        webTestClient.post()
            .uri("/api/v1/namespaces/NOT_FOUND")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("$.message")
            .value(Matchers.containsString("Operation not supported, storage disabled"))
    }
}