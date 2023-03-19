package com.go.feature.controller.dto.featuretoggle

data class FeatureToggleRequest(
    val namespace: String?,
    val data: List<DataItem>
) {
    data class DataItem(
        val parameter: String,
        val value: String
    )
}