package com.go.feature.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.go.feature.converter.util.getFilterIdByName
import com.go.feature.dto.settings.loader.LoadedSettings
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.util.randomId
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
}