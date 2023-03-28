package com.go.feature.dto.settings.loader

import com.go.feature.dto.operator.FilterOperator
import com.go.feature.dto.status.FilterStatus
import com.go.feature.dto.status.Status

data class LoadedSettings(
    val namespace: Namespace,
    val filters: List<Filter>,
    val features: List<Feature>,
) {
    data class Namespace(
        val name: String,
        val status: Status = Status.ENABLED,
    )

    data class Filter(
        val name: String,
        val parameter: String,
        val operator: FilterOperator,
        val status: FilterStatus = FilterStatus.ENABLED,
        val description: String?,
    )

    data class Feature(
        val name: String,
        val status: Status = Status.ENABLED,
        val filters: List<FeatureFilter>,
        val description: String?,
    )

    data class FeatureFilter(
        val name: String,
        val value: String,
    )
}