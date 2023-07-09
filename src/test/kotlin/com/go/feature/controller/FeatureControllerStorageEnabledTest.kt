package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.feature.FeatureCreateRequest
import com.go.feature.controller.dto.feature.FeatureEditRequest
import com.go.feature.controller.dto.feature.FeatureResponse
import com.go.feature.controller.dto.feature.FeaturesResponse
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.dto.status.Status
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.web.util.UriBuilder

@TestPropertySource(properties = [
    "spring.config.location = classpath:application-storage-enabled.yml"
])
class FeatureControllerStorageEnabledTest : WebIntegrationTest() {
    @Test
    fun createFeatureTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
        createFeature(namespace.id, "createdFeature")
    }

    @Test
    fun createAlreadyExistedFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
        createFeature(namespace.id, "createdForExistCheckFeature")

        val request = FeatureCreateRequest(
            name = "createdForExistCheckFeature",
            status = Status.ENABLED,
            namespace = namespace.id,
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
            .jsonPath("message").isEqualTo("Feature already exists")
    }

    @Test
    fun editFilterTest() {
        runBlocking {
            val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
            val feature: FeatureResponse = createFeature(namespace.id, "editedFilter")

            val editRequest = FeatureEditRequest(
                name = "editedFeature",
                status = Status.DISABLED,
                filters = emptyList(),
                description = "feature description edited",
                version = feature.version
            )

            val editedFeature: FeatureResponse = webTestClient.post()
                .uri("/api/v1/features/${feature.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editRequest)
                .exchange()
                .expectStatus().isOk
                .returnResult(FeatureResponse::class.java)
                .responseBody
                .awaitFirst()
                ?: fail("Feature was not returned")

            Assertions.assertNotNull(editedFeature)
            Assertions.assertEquals(editRequest.name, editedFeature.name)
            Assertions.assertEquals(editRequest.status, editedFeature.status)
            // TODO: assert filters equals
            Assertions.assertEquals(editRequest.description, editedFeature.description)
            Assertions.assertEquals(editRequest.version + 1, editedFeature.version)

            webTestClient.get()
                .uri("/api/v1/features/${editedFeature.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(feature.id)
                .jsonPath("$.name").isEqualTo(editRequest.name)
                .jsonPath("$.status").isEqualTo(editRequest.status.name)
                // TODO: assert filters equals
                .jsonPath("$.description").isEqualTo(editRequest.description!!)
                .jsonPath("$.version").isEqualTo(editedFeature.version)


            // update with outdated version
            val editRequestOutdated = FeatureEditRequest(
                name = "editedFilter",
                status = Status.DISABLED,
                filters = emptyList(),
                description = "filter description edited",
                version = feature.version
            )

            webTestClient.post()
                .uri("/api/v1/features/${feature.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editRequestOutdated)
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .jsonPath("$.message")
                .value(Matchers.containsString("Failed to update table [features]. Version does not match for row with Id"))
        }
    }

    @Test
    fun editedFeatureNotFoundTest() {
        val editRequest = FeatureEditRequest(
            name = "editedFeature",
            status = Status.DISABLED,
            filters = emptyList(),
            description = "feature description edited",
            version = 0
        )

        webTestClient.post()
            .uri("/api/v1/features/NOT_FOUND")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(editRequest)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Feature not found")
    }

    @Test
    fun deleteFeatureTest(): Unit = runBlocking {
        deleteFeatureSuccess(NAMESPACE_NAME)
    }

    @Test
    fun deleteNotFoundFeatureTest(): Unit = runBlocking {
        webTestClient.delete()
            .uri("/api/v1/features/NOT_FOUND_ID")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message")
            .isEqualTo("Feature not found")
    }

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
            ?: fail("Namespace was not found")
    }

    private suspend fun getFeature(namespaceId: String, featureName: String): FeatureResponse {
        val response: FeaturesResponse = webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("/api/v1/features")
                    .queryParam("ns", namespaceId)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .returnResult(FeaturesResponse::class.java)
            .responseBody
            .awaitSingle()

        return response.features
            .find { it.name == featureName }
            ?: fail("Feature was not found")
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

    private fun createFeature(namespaceId: String, featureName: String): FeatureResponse = runBlocking {
        val request = FeatureCreateRequest(
            name = featureName,
            status = Status.ENABLED,
            namespace = namespaceId,
            filters = emptyList(),
            // TODO: create filters
            description = "feature description",
        )

        val response: FeatureResponse = webTestClient.post()
            .uri("/api/v1/features")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .returnResult(FeatureResponse::class.java)
            .responseBody
            .awaitFirst()
            ?: fail("Feature was not returned")

        Assertions.assertEquals(request.name, response.name)
        Assertions.assertEquals(request.status, response.status)
        Assertions.assertEquals(request.namespace, response.namespace)
        // TODO: create filters
        Assertions.assertEquals(request.description, response.description)

        webTestClient.get()
            .uri("/api/v1/features/${response.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.name").isEqualTo(request.name)
            .jsonPath("$.status").isEqualTo(request.status.name)
            .jsonPath("$.namespace").isEqualTo(request.namespace)
            // TODO: create filters
            .jsonPath("$.description").isEqualTo(request.description!!)

        response
    }

    private suspend fun deleteFeatureSuccess(namespaceName: String) {
        val namespace: NamespaceResponse = getNamespace(namespaceName)
        val feature: FeatureResponse = getFeature(namespace.id, "forDeleteFeature")

        webTestClient.delete()
            .uri("/api/v1/features/${feature.id}")
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/api/v1/features/${feature.id}")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Feature not found")
    }

    private companion object {
        private const val NAMESPACE_NAME = "feature-test"
    }
}