package com.go.feature.service.dto.loader

data class LoadedSettings(
    val namespace: Namespace,
    val filters: List<Filter>,
    val features: List<Features>,
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

    data class Features(
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