package com.markklim.feature.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.markklim.feature.controller.dto.feature.FeatureCreateRequest
import com.markklim.feature.controller.dto.feature.FeatureEditRequest
import com.markklim.feature.controller.dto.feature.FeatureResponse
import com.markklim.feature.converter.util.getFilterIdByName
import com.markklim.feature.dto.settings.loader.LoadedSettings
import com.markklim.feature.persistence.entity.Feature
import com.markklim.feature.persistence.entity.Filter
import com.markklim.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class FeatureConverter(
    val objectMapper: ObjectMapper
) {

    fun create(
        namespaceId: String,
        featureSettings: List<LoadedSettings.Feature>,
        nameToFilterMap: Map<String, Filter>
    ): List<Feature> =
        featureSettings.map {
            val featureFilters: List<Feature.Filter> = it.filters.map { filter ->
                Feature.Filter(
                    id = getFilterIdByName(nameToFilterMap, filter.name),
                    value = filter.value
                )
            }

            Feature(
                id = randomId(),
                name = it.name,
                namespace = namespaceId,
                filters = objectMapper.writeValueAsString(featureFilters),
                status = it.status,
                description = it.description
            )
        }

    fun create(request: FeatureCreateRequest): Feature =
        Feature(
            id = randomId(),
            name = request.name,
            namespace = request.namespace,
            filters = objectMapper.writeValueAsString(request.filters),
            status = request.status,
            description = request.description
        )

    fun convert(feature: Feature): FeatureResponse =
        FeatureResponse(
            id = feature.id,
            name = feature.name,
            namespace = feature.namespace,
            filters = objectMapper.readValue(feature.filters),
            status = feature.status,
            description = feature.description,
            version = feature.version!!
        )

    fun edit(editedFeature: Feature, request: FeatureEditRequest): Feature =
        editedFeature.copy(
            name = request.name,
            filters = objectMapper.writeValueAsString(request.filters),
            status = request.status,
            description = request.description,
            version = request.version,
        )

    fun getFeatureFilter(feature: Feature): List<Feature.Filter> = objectMapper.readValue(feature.filters)
}