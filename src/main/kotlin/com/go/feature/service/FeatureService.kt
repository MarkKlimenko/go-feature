package com.go.feature.service

import com.go.feature.converter.FeatureConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FeatureRepository
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

@Service
class FeatureService(
    val featureRepository: FeatureRepository,
    val featureConverter: FeatureConverter,
) {
    suspend fun deleteAllForNamespace(namespaceId: String) {
        featureRepository.deleteAllByNamespace(namespaceId)
    }

    suspend fun createFeaturesForSettings(namespaceId: String, settings: LoadedSettings, filters: List<Filter>) {
        val nameToFilterMap: Map<String, Filter> = filters.associateBy { it.name }
        val features: List<Feature> = featureConverter.create(namespaceId, settings.features, nameToFilterMap)
        featureRepository.saveAll(features).collect()
    }
}