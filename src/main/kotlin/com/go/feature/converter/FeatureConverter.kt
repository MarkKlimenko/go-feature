package com.go.feature.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.service.loader.dto.LoadedSettings
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
    ): List<Feature> {
        return featureSettings.map {
            val featureFilters: List<Feature.Filter> = it.filters.map { filter ->
                Feature.Filter(
                    id = nameToFilterMap[filter.name]?.id
                        ?: throw IllegalArgumentException("No filter with name=${filter.name}"),
                    value = filter.value
                )
            }

            Feature(
                id = randomId(),
                name = it.name,
                namespace = namespaceId,
                filters = objectMapper.writeValueAsString(featureFilters),
                status = convertStatus(it.status),
                description = it.description
            )
        }
    }

    fun convertStatus(status: LoadedSettings.Status): Feature.Status {
        return when (status) {
            LoadedSettings.Status.ENABLED -> Feature.Status.ENABLED
            LoadedSettings.Status.DISABLED -> Feature.Status.DISABLED
        }
    }
}