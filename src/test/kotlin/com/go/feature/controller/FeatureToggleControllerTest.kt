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
        "contains: multiple values, contains_multi",
        "eq: simple, eq",
        "more_less: simple, more_less",
        "version: one of params (ios), version_ios",
        "version: one of params (android), version_android",
        "mix: no features for request, mix_no_features",
        "mix: empty data, mix_empty_data",
    )
    fun ftTest(description: String, type: String) {
        webTestClient.post()
            .uri("/api/v1/features/search")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(fileToString("feature/${type}_ft_request.json"))
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(fileToString("feature/${type}_ft_response.json"))
    }

    // TODO: check empty default namespace
}