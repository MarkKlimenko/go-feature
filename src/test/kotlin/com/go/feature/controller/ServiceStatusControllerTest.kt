package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import org.junit.jupiter.api.Test

class ServiceStatusControllerTest : WebIntegrationTest() {
    @Test
    fun checkLivenessTest() {
        webTestClient.get()
            .uri("/service/liveness")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }

    @Test
    fun checkReadinessTest() {
        webTestClient.get()
            .uri("/service/readiness")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }
}