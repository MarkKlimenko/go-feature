package com.go.feature.controller

import com.go.feature.controller.dto.filter.FilterCreateRequest
import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class FilterControllerStorageDisabledTest : EntityManipulationTest() {

    @Test
    fun createFilterNotAllowedTest(): Unit = runBlocking {
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
    fun editFilterNotAllowedTest(): Unit = runBlocking {
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
}