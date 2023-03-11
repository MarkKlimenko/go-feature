package com.go.feature.entity

data class Filter(
    val id: String,
    val namespace: String,
    val parameter: String,
    val operator: String,
    val description: String,
)
