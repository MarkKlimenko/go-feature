package com.go.feature.controller

import com.go.feature.controller.dto.feature.FeatureCreateRequest
import com.go.feature.dto.status.Status
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class FeatureControllerStorageDisabledTest : EntityManipulationTest() {

    @Test
    fun createFeatureNotAllowedTest(): Unit = runBlocking {
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
}