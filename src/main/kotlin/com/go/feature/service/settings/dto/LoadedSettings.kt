package com.go.feature.service.settings.dto

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
        val operator: String,
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

    enum class Status {
        ENABLED, DISABLED
    }
}