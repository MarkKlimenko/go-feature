package com.go.feature.service.feature

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.controller.dto.feature.FeaturesFindRequest
import com.go.feature.controller.dto.feature.FeaturesFindResponse
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.index.IndexService
import com.go.feature.util.exception.localized.ClientException
import com.go.feature.util.message.NAMESPACE_NOT_FOUND_ERROR
import org.springframework.stereotype.Service

@Service
class FeatureSearchService(
    val applicationProperties: ApplicationProperties,
    val namespaceRepository: NamespaceRepository,
    val indexService: IndexService,
) {

    suspend fun findFeatures(request: FeaturesFindRequest): FeaturesFindResponse {
        val namespaceName: String = request.namespace ?: applicationProperties.namespace.default
        // TODO: create load test with this line and without
        val namespace: Namespace = namespaceRepository.findByName(namespaceName)
            ?: throw ClientException(NAMESPACE_NOT_FOUND_ERROR)

        return FeaturesFindResponse(
            features = indexService.getFeaturesFromIndex(namespace.id, request.data)
        )
    }
}