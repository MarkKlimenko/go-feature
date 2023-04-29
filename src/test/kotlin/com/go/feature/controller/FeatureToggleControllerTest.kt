package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.test.utils.fileToString
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.MediaType

class FeatureToggleControllerTest : WebIntegrationTest() {

    @ParameterizedTest
    @CsvSource(
        "contains: simple, contains",
        "contains: multiple value, contains_multi",
        "eq: simple, eq",
    )
    fun ftTest(description: String, type: String) {
        webTestClient.post()
            .uri("/api/v1/feature-toggle/find")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(fileToString("feature/${type}_ft_request.json"))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(fileToString("feature/${type}_ft_response.json"))
    }
}