package com.go.feature.controller.dto.feature

data class FeaturesFindRequest(
    val namespace: String?,
    val data: List<DataItem>
) {
    data class DataItem(
        val parameter: String,
        val value: String
    )
}