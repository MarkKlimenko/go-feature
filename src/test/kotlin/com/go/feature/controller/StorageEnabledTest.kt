package com.go.feature.controller

import com.go.feature.WebIntegrationTest
import com.go.feature.controller.dto.feature.FeatureResponse
import com.go.feature.controller.dto.feature.FeaturesResponse
import com.go.feature.controller.dto.filter.FilterResponse
import com.go.feature.controller.dto.filter.FiltersResponse
import com.go.feature.controller.dto.namespace.NamespaceResponse
import com.go.feature.controller.dto.namespace.NamespacesResponse
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions
import org.springframework.web.util.UriBuilder

class StorageEnabledTest : WebIntegrationTest() {
    suspend fun getNamespace(namespaceName: String): NamespaceResponse {
        val nsResponse: NamespacesResponse = webTestClient.get()
            .uri("/api/v1/namespaces")
            .exchange()
            .expectStatus().isOk
            .returnResult(NamespacesResponse::class.java)
            .responseBody
            .awaitSingle()

        return nsResponse.namespaces
            .find { it.name == namespaceName }
            ?: Assertions.fail("Namespace was not found")
    }

    suspend fun getFilter(namespaceId: String, filterName: String): FilterResponse {
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
            ?: Assertions.fail("Filter was not found")
    }

    suspend fun getFeature(namespaceId: String, featureName: String): FeatureResponse {
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
            ?: Assertions.fail("Feature was not found")
    }
}