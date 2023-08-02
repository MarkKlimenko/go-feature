package com.markklim.feature.service.feature

import com.markklim.feature.configuration.properties.ApplicationProperties
import com.markklim.feature.controller.dto.feature.FeaturesFindRequest
import com.markklim.feature.controller.dto.feature.FeaturesFindResponse
import com.markklim.feature.persistence.entity.Namespace
import com.markklim.feature.persistence.repository.NamespaceRepository
import com.markklim.feature.service.index.IndexService
import com.markklim.feature.util.exception.client.ClientException
import com.markklim.feature.util.message.NAMESPACE_NOT_FOUND_ERROR
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