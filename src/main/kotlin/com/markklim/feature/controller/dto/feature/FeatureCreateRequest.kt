package com.markklim.feature.controller.dto.feature

import com.markklim.feature.dto.status.Status
import com.markklim.feature.persistence.entity.Feature
import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.NotBlank

data class FeatureCreateRequest(
    @field:Length(min = 1, max = 100)
    @field:NotBlank
    val name: String,

    @field:Length(min = 1, max = 100)
    @field:NotBlank
    val namespace: String,

    val filters: List<Feature.Filter>,

    val status: Status,

    val description: String? = null,
)
