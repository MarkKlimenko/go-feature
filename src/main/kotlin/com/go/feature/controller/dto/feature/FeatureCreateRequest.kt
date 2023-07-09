package com.go.feature.controller.dto.feature

import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Feature
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.NotBlank

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
