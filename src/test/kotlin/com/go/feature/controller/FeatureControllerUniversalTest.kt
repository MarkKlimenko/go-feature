package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import com.go.feature.test.utils.assertContains
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.web.util.UriBuilder

@ExtendWith(OutputCaptureExtension::class)
class FeatureControllerUniversalTest : WebIntegrationTest() {
    @Test
    fun getFeaturesTest(): Unit = runBlocking {
        val nsResponse: NamespacesResponse = webTestClient.get()
            .uri("/api/v1/namespaces")
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespacesResponse::class.java)
            .responseBody
            .awaitSingle()

        val namespaceResponse: NamespaceResponse = nsResponse.namespaces
            .find { it.name == "feature-test" }
            ?: Assertions.fail("Namespace was not found")

        webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path("/api/v1/features")
                    .queryParam("ns", namespaceResponse.id)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.features[0].id").isNotEmpty
            .jsonPath("$.features[0].name").isNotEmpty
            .jsonPath("$.features[0].status").isNotEmpty
            .jsonPath("$.features[0].filters").isNotEmpty
            .jsonPath("$.features[0].description").isNotEmpty
            .jsonPath("$.features[0].version").isNotEmpty
    }

    @Test
    fun getNotFoundFeatureTest(output: CapturedOutput) {
        webTestClient.get()
            .uri("/api/v1/features/NOT_FOUND")
            .header("X-B3-TraceId", "d61436368bae3c12")
            .header("X-B3-SpanId", "ce5f844337f3ee88")
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody()
            .jsonPath("message").isEqualTo("Feature not found")

        output.assertContains("d61436368bae3c12")
        output.assertContains("ce5f844337f3ee88")
    }
}