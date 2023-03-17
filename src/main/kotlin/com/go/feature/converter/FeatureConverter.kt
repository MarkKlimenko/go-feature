package com.go.feature.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.go.feature.persistence.entity.Feature
import com.go.feature.persistence.entity.Filter
import com.go.feature.service.settings.dto.LoadedSettings
import com.go.feature.util.randomId
import org.springframework.stereotype.Component

@Component
class FeatureConverter(
    val objectMapper: ObjectMapper
) {

    fun create(
        namespaceId: String,
        featureSetting: LoadedSettings.Feature,
        nameToFilterMap: Map<String, Filter>
    ): Feature {
        val featureFilters: List<Feature.Filter> = featureSetting.filters.map { filter ->
            Feature.Filter(
                id = nameToFilterMap[filter.name]?.id
                    ?: throw IllegalArgumentException("No filter with name=${filter.name}"),
                value = filter.value
            )
        }

        return Feature(
            id = randomId(),
            name = featureSetting.name,
            namespace = namespaceId,
            filters = objectMapper.writeValueAsString(featureFilters),
            status = convertStatus(featureSetting.status),
            description = featureSetting.description
        )
    }

    fun convertStatus(status: LoadedSettings.Status): Feature.Status {
        return when (status) {
            LoadedSettings.Status.ENABLED -> Feature.Status.ENABLED
            LoadedSettings.Status.DISABLED -> Feature.Status.DISABLED
        }
    }
}