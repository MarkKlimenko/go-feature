package com.go.feature.controller.dto.feature

import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Feature

data class FeatureResponse(
    val id: String,
    val name: String,
    val namespace: String,
    val filters: List<Feature.Filter>,
    val status: Status,
    val description: String? = null,
    val version: Int
)
