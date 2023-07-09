package com.go.feature.controller

import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.controller.dto.filter.FilterEditRequest
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
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
class FilterControllerStorageEnabledTest : EntityManipulationTest() {
    @Test
    fun createFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
        createFilter(namespace.id, "createdFilter")
    }

    @Test
    fun createAlreadyExistedFilterTest(): Unit = runBlocking {
        val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
        createFilter(namespace.id, "createdForExistCheckFilter")

        val request = FilterCreateRequest(
            name = "createdForExistCheckFilter",
            status = FilterStatus.ENABLED,
            namespace = namespace.id,
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
            .jsonPath("message").isEqualTo("Filter already exists")
    }

    @Test
    fun editFilterTest() {
        runBlocking {
            val namespace: NamespaceResponse = getNamespace(NAMESPACE_NAME)
            val filter: FilterResponse = createFilter(namespace.id, "editedFilter")

            val editRequest = FilterEditRequest(
                name = "editedFilter",
                status = FilterStatus.DISABLED,
                parameter = "testParameterEdited",
                operator = FilterOperator.CONTAINS,
                description = "filter description edited",
                version = filter.version
            )

            val editedFilter: FilterResponse = webTestClient.post()
                .uri("/api/v1/filters/${filter.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editRequest)
                .exchange()
                .expectStatus().isOk
                .returnResult(FilterResponse::class.java)
                .responseBody
                .awaitFirst()
                ?: fail("Filter was not returned")

            Assertions.assertNotNull(editedFilter)
            Assertions.assertEquals(editRequest.name, editedFilter.name)
            Assertions.assertEquals(editRequest.status, editedFilter.status)
            Assertions.assertEquals(editRequest.parameter, editedFilter.parameter)
            Assertions.assertEquals(editRequest.operator, editedFilter.operator)
            Assertions.assertEquals(editRequest.description, editedFilter.description)
            Assertions.assertEquals(editRequest.version + 1, editedFilter.version)

            webTestClient.get()
                .uri("/api/v1/filters/${editedFilter.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isEqualTo(filter.id)
                .jsonPath("$.name").isEqualTo(editRequest.name)
                .jsonPath("$.status").isEqualTo(editRequest.status.name)
                .jsonPath("$.parameter").isEqualTo(editRequest.parameter)
                .jsonPath("$.operator").isEqualTo(editRequest.operator.name)
                .jsonPath("$.description").isEqualTo(editRequest.description!!)
                .jsonPath("$.version").isEqualTo(editedFilter.version)


            // update with outdated version
            val editRequestOutdated = FilterEditRequest(
                name = "editedFilter",
                status = FilterStatus.DISABLED,
                parameter = "testParameterEdited",
                operator = FilterOperator.CONTAINS,
                description = "filter description edited",
                version = filter.version
            )

            webTestClient.post()
                .uri("/api/v1/filters/${filter.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editRequestOutdated)
                .exchange()
                .expectStatus().is4xxClientError
                .expectBody()
                .jsonPath("$.message")
                .value(Matchers.containsString("Failed to update table [filters]. Version does not match for row with Id"))
        }
    }

    @Test
    fun editedFilterNotFoundTest() {
        val editRequest = FilterEditRequest(
            name = "editedFilter",
            status = FilterStatus.DISABLED,
            parameter = "testParameterEdited",
            operator = FilterOperator.CONTAINS,
            description = "filter description edited",
            version = 0
        )

        webTestClient.post()
            .uri("/api/v1/filters/NOT_FOUND")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(editRequest)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Filter not found")
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

    private fun createFilter(namespaceId: String, filterName: String): FilterResponse = runBlocking {
        val request = FilterCreateRequest(
            name = filterName,
            status = FilterStatus.ENABLED,
            namespace = namespaceId,
            parameter = "${filterName}testParameter",
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