package com.go.feature.service.feature

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.go.feature.converter.FeatureConverter
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.persistence.repository.FeatureRepository
import com.go.feature.util.exception.ValidationException
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

@Service
class FeatureService(
    val featureRepository: FeatureRepository,
    val featureConverter: FeatureConverter,
    val objectMapper: ObjectMapper,
) {
    suspend fun deleteAllForNamespace(namespaceId: String) {
        featureRepository.deleteAllByNamespace(namespaceId)
    }

    suspend fun validateFilterNotUsedByFeatures(filter: Filter) {
        // TODO: use pagination
        featureRepository.findByNamespace(filter.namespace)
            .collect { feature ->
                val featureFilters: List<Feature.Filter> = objectMapper.readValue(feature.filters)
                featureFilters.forEach {
                    if (it.id == filter.id) {
                        throw ValidationException("Filter '${filter.name}' is used at most by one feature '${feature.name}'")
                    }
                }
            }
    }

    suspend fun createFeaturesForSettings(namespaceId: String, settings: LoadedSettings, filters: List<Filter>) {
        val nameToFilterMap: Map<String, Filter> = filters.associateBy { it.name }
        val features: List<Feature> = featureConverter.create(namespaceId, settings.features, nameToFilterMap)
        featureRepository.saveAll(features).collect()
    }
}