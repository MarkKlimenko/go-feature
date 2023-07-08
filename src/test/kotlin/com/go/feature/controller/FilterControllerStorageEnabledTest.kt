package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.web.util.UriBuilder

@TestPropertySource(properties = [
    "spring.config.location = classpath:application-storage-enabled.yml"
])
class FilterControllerStorageEnabledTest : WebIntegrationTest() {
    @Test
    fun createFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
        createFilter(namespace.id, "createdFilter")
    }

    @Test
    fun deleteFilterTest(): Unit = runBlocking {
        deleteFilterSuccess(NAMESPACE_NAME)
    }

    @Test
    fun deleteFilterWithoutFeaturesTest(): Unit = runBlocking {
        deleteFilterSuccess(NAMESPACE_NO_FEATURES_NAME)
    }

    @Test
    fun deleteUsedFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
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

    @Test
    fun deleteNotFoundFilterTest(): Unit = runBlocking {
        webTestClient.delete()
            .uri("/api/v1/filters/NOT_FOUND_ID")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message")
            .isEqualTo("Filter not found")
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

    private fun createFilter(namespaceId: String, filterName: String): FilterResponse = runBlocking {
        val request = FilterCreateRequest(
            name = filterName,
            status = FilterStatus.ENABLED,
            namespace = namespaceId,
            parameter = "testParameter",
            operator = FilterOperator.EQ,
            description = "filter description",
        )

        val response: FilterResponse = webTestClient.post()
            .uri("/api/v1/filters")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .returnResult(FilterResponse::class.java)
            .responseBody
            .awaitFirst()
            ?: fail("Filter was not returned")

        Assertions.assertEquals(request.name, response.name)
        Assertions.assertEquals(request.status, response.status)
        Assertions.assertEquals(request.namespace, response.namespace)
        Assertions.assertEquals(request.parameter, response.parameter)
        Assertions.assertEquals(request.operator, response.operator)
        Assertions.assertEquals(request.description, response.description)

        webTestClient.get()
            .uri("/api/v1/filters/${response.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.name").isEqualTo(request.name)
            .jsonPath("$.status").isEqualTo(request.status.name)
            .jsonPath("$.namespace").isEqualTo(request.namespace)
            .jsonPath("$.parameter").isEqualTo(request.parameter)
            .jsonPath("$.operator").isEqualTo(request.operator.name)
            .jsonPath("$.description").isEqualTo(request.description!!)

        response
    }

    private suspend fun deleteFilterSuccess(namespaceName: String) {
        val namespace: NamespaceResponse = getNamespace(namespaceName)
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

    private companion object {
        private const val NAMESPACE_NAME = "filter-test"
        private const val NAMESPACE_NO_FEATURES_NAME = "filter-no-features-test"
    }
}