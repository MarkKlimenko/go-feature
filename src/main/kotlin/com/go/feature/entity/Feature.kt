package com.go.feature.entity

data class Feature(
    val id: String,
    val namespace: String,
    val name: String,
    val filters: List<Filter>,
    val status: Status,
    val description: String,
) {
    data class Filter(
        val id: String,
        val value: String,
    )

    enum class Status {
        ENABLED, DISABLED
    }
}
