package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.namespace.NamespaceCreateRequest
import com.go.feature.controller.dto.namespace.NamespaceEditRequest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.IndexVersion
import com.go.feature.service.index.IndexVersionService
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = [
    "spring.config.location = classpath:application-storage-enabled.yml"
])
class NamespaceControllerStorageEnabledTest : WebIntegrationTest() {
    @Autowired
    lateinit var indexVersionService: IndexVersionService

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
    fun createNamespaceBlankValidationTest() {
        val request = NamespaceCreateRequest(
            name = " ",
            status = Status.ENABLED
        )

        webTestClient.post()
            .uri("/api/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Validation exception")
            .jsonPath("validations.name").isEqualTo("must not be blank")
    }

    @Test
    fun createNamespaceNullValidationTest() {
        val request: Map<String, String> = mapOf(
            "status" to Status.ENABLED.name
        )

        webTestClient.post()
            .uri("/api/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Validation exception")
            .jsonPath("validations.name").isEqualTo("must not be null")
    }

    @Test
    fun editNamespaceTest() {
        runBlocking {
            val namespace: NamespaceResponse = createNamespace("forEditingNamespace")

            val indexVersionBeforeUpdate: IndexVersion = indexVersionService.find(namespace.id)
                ?: fail("Index version before update is null")

            val editRequest = NamespaceEditRequest(
                name = "editedNamespace",
                status = Status.DISABLED,
                version = namespace.version
            )

            val editedNamespace: NamespaceResponse = webTestClient.post()
                .uri("/api/v1/namespaces/${namespace.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editRequest)
                .exchange()
                .expectStatus().isOk
                .returnResult(NamespaceResponse::class.java)
                .responseBody
                .awaitFirst()
                ?: fail("Namespace was not returned")

            assertNotNull(editedNamespace)
            assertEquals(editRequest.name, editedNamespace.name)
            assertEquals(editRequest.status, editedNamespace.status)

            webTestClient.get()
                .uri("/api/v1/namespaces/${editedNamespace.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(namespace.id)
                .jsonPath("$.name").isEqualTo(editRequest.name)
                .jsonPath("$.status").isEqualTo(editRequest.status.name)

            val indexVersionAfterUpdate: IndexVersion = indexVersionService.find(editedNamespace.id)
                ?: fail("Index version before update is null")

            assertTrue(indexVersionBeforeUpdate.indexVersionValue != indexVersionAfterUpdate.indexVersionValue)

            // update with outdated version
            val editRequestOutdated = NamespaceEditRequest(
                name = "editedNamespace",
                status = Status.DISABLED,
                version = namespace.version
            )

            webTestClient.post()
                .uri("/api/v1/namespaces/${namespace.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editRequestOutdated)
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .jsonPath("$.message")
                .value(Matchers.containsString("Failed to update table [namespaces]. Version does not match for row with Id"))
        }
    }

    @Test
    fun editedNamespaceNotFoundTest() {
        val request = NamespaceEditRequest(
            name = "NOT_FOUND",
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
            .jsonPath("message").isEqualTo("Namespace not found")
    }

    @Test
    fun deleteNamespaceTest() {
        runBlocking {
            val response: NamespacesResponse = webTestClient.get()
                .uri("/api/v1/namespaces")
                .exchange()
                .expectStatus().isOk
                .returnResult(NamespacesResponse::class.java)
                .responseBody
                .awaitSingle()

            val namespace: NamespaceResponse = response.namespaces
                .find { it.name == "for-delete" }
                ?: fail("Namespace was not found")

            webTestClient.delete()
                .uri("/api/v1/namespaces/${namespace.id}")
                .exchange()
                .expectStatus().isOk

            webTestClient.get()
                .uri("/api/v1/namespaces/${namespace.id}")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .jsonPath("message").isEqualTo("Namespace not found")
        }
    }

    @Test
    fun deleteNotFoundNamespaceTest() {
        runBlocking {
            webTestClient.delete()
                .uri("/api/v1/namespaces/NOT_FOUND_ID")
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .jsonPath("message").isEqualTo("Namespace not found")
        }
    }

    private fun createNamespace(name: String): NamespaceResponse = runBlocking {
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
            .awaitFirst()
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

        indexVersionService.find(response.id)
            ?: fail("Index version is null")

        response
    }
}