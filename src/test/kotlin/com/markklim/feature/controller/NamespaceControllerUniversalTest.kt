package com.markklim.feature.controller

import com.markklim.feature.WebIntegrationTest
import org.junit.jupiter.api.Test

class NamespaceControllerUniversalTest : WebIntegrationTest() {
    @Test
    fun getNamespacesTest() {
        webTestClient.get()
            .uri("/api/v1/namespaces")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.namespaces[0].id").isNotEmpty
            .jsonPath("$.namespaces[0].name").isNotEmpty
            .jsonPath("$.namespaces[0].status").isNotEmpty
    }

    @Test
    fun getNotFoundNamespaceTest() {
        webTestClient.get()
            .uri("/api/v1/namespaces/NOT_FOUND")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Namespace not found")
    }
}