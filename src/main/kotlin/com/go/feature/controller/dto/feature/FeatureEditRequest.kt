package com.go.feature.controller.dto.feature

import com.go.feature.dto.status.Status
import com.go.feature.persistence.entity.Feature
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class FeatureEditRequest(
    @field:Length(min = 1, max = 100)
    @field:NotBlank
    val name: String,

    val filters: List<Feature.Filter>,

    val status: Status,

    val description: String? = null,

    @field:Min(0)
    val version: Int,
)
