package com.markklim.feature.test.utils.setting

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.feature.dto.operator.FilterOperator
import com.markklim.feature.dto.settings.loader.LoadedSettings

class SettingsProviderUtil {

    fun generateSettings(
        namespaceName: String,
        filtersSize: Int,
        featuresSize: Int,
        filtersInFeature: Int = 1
    ): ByteArray {
        val namespace = LoadedSettings.Namespace(
            name = namespaceName
        )

        val filters: List<LoadedSettings.Filter> = (1..filtersSize).map {
            LoadedSettings.Filter(
                name = "filter$it",
                parameter = "parameter$it",
                operator = FilterOperator.EQ
            )
        }

        val features: List<LoadedSettings.Feature> = (1..featuresSize).map { featureCount ->
            LoadedSettings.Feature(
                name = "feature$featureCount",
                filters = (1..filtersInFeature).map {
                    LoadedSettings.FeatureFilter(
                        name = filters[it].name,
                        value = "value",
                    )
                }
            )
        }

        val settings = LoadedSettings(
            namespace = namespace,
            filters = filters,
            features = features,
        )

        return ObjectMapper().writeValueAsBytes(settings)
    }
}