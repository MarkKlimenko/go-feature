package com.go.feature.service

import com.go.feature.configuration.properties.ApplicationProperties
import com.go.feature.controller.dto.featuretoggle.FeatureToggleRequest
import com.go.feature.controller.dto.featuretoggle.FeatureToggleResponse
import com.go.feature.persistence.entity.Namespace
import com.go.feature.persistence.repository.NamespaceRepository
import com.go.feature.service.index.IndexService
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    val applicationProperties: ApplicationProperties,
    val namespaceRepository: NamespaceRepository,
    val indexService: IndexService,
) {

    suspend fun findFeatureToggles(request: FeatureToggleRequest): FeatureToggleResponse {
        val namespaceName: String = request.namespace ?: applicationProperties.namespace.default
        val namespace: Namespace = namespaceRepository.findByName(namespaceName)
            ?: throw IllegalArgumentException("Namespace '${namespaceName}' not found")

        return FeatureToggleResponse(
            features = indexService.getFeaturesFromIndex(namespace.id, request.data)
        )
    }
}