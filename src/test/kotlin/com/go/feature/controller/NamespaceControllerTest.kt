package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.dto.status.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class NamespaceControllerTest : WebIntegrationTest() {
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

    @Test
    fun createNamespaceTest() {
        createNamespace("newNamespace")
    }

    @Test
    fun createAlreadyExistedNamespaceTest() {
        val request = NamespaceCreateRequest(
            name = "eq",
            status = Status.ENABLED
        )

        webTestClient.post()
            .uri("/api/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Namespace already exists")
    }

    @Test
    fun editNamespaceTest() {
        val editedNamespace: NamespaceResponse = createNamespace("forEditingNamespace")

        val request = NamespaceEditRequest(
            name = "editedNamespace",
            status = Status.DISABLED
        )

        val response: NamespaceResponse = webTestClient.post()
            .uri("/api/v1/namespaces/${editedNamespace.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespaceResponse::class.java)
            .responseBody
            .blockFirst()
            ?: fail("Namespace was not returned")

        assertNotNull(response)
        assertEquals(request.name, response.name)
        assertEquals(request.status, response.status)

        webTestClient.get()
            .uri("/api/v1/namespaces/${response.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(editedNamespace.id)
            .jsonPath("$.name").isEqualTo(request.name)
            .jsonPath("$.status").isEqualTo(request.status.name)
    }

    @Test
    fun editedNamespaceNotFoundTest() {
        val request = NamespaceEditRequest(
            name = "NOT_FOUND",
            status = Status.DISABLED
        )

        webTestClient.post()
            .uri("/api/v1/namespaces/NOT_FOUND")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Namespace not found")
    }

    private fun createNamespace(name: String): NamespaceResponse {
        val request = NamespaceCreateRequest(
            name = name,
            status = Status.ENABLED
        )

        val response: NamespaceResponse = webTestClient.post()
            .uri("/api/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespaceResponse::class.java)
            .responseBody
            .blockFirst()
            ?: fail("Namespace was not returned")

        assertEquals(request.name, response.name)
        assertEquals(request.status, response.status)

        webTestClient.get()
            .uri("/api/v1/namespaces/${response.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.name").isEqualTo(request.name)
            .jsonPath("$.status").isEqualTo(request.status.name)

        return response
    }
}