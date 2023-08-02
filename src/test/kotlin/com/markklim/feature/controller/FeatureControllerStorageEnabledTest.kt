package com.markklim.feature.controller

import com.markklim.feature.controller.dto.feature.FeatureCreateRequest
import com.markklim.feature.controller.dto.feature.FeatureEditRequest
import com.markklim.feature.controller.dto.feature.FeatureResponse
import com.markklim.feature.controller.dto.filter.FilterResponse
import com.markklim.feature.controller.dto.namespace.NamespaceResponse
import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Feature
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = [
    "spring.config.location = classpath:application-storage-enabled.yml"
])
class FeatureControllerStorageEnabledTest : EntityManipulationTest() {
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

            val filter: FilterResponse = getFilter(namespace.id, "userNameContains")

            val filters: List<Feature.Filter> = listOf(
                Feature.Filter(
                    id = filter.id,
                    value = "testValue"
                )
            )

            val editRequest = FeatureEditRequest(
                name = "editedFeature",
                status = Status.DISABLED,
                filters = filters,
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
            Assertions.assertEquals(editRequest.filters, editedFeature.filters)
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
                .jsonPath("$.filters[0].id").isEqualTo(editRequest.filters[0].id)
                .jsonPath("$.filters[0].value").isEqualTo(editRequest.filters[0].value)
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

    private fun createFeature(namespaceId: String, featureName: String): FeatureResponse = runBlocking {
        val filter: FilterResponse = getFilter(namespaceId, "userNameEq")

        val filters: List<Feature.Filter> = listOf(
            Feature.Filter(
                id = filter.id,
                value = "testValue"
            )
        )

        val request = FeatureCreateRequest(
            name = featureName,
            status = Status.ENABLED,
            namespace = namespaceId,
            filters = filters,
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
        Assertions.assertEquals(request.filters, response.filters)
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
            .jsonPath("$.filters[0].id").isEqualTo(request.filters[0].id)
            .jsonPath("$.filters[0].value").isEqualTo(request.filters[0].value)
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